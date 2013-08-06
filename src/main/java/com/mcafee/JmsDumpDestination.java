package com.mcafee;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class writes the contents of a JMS destination (Queue, Topic and DurableSubscribers) \
 * to the local file system using the JMSWriter object. 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */

public class JmsDumpDestination implements MessageListener {
	private static final Logger LOG = LoggerFactory.getLogger(JmsDumpDestination.class);
	private static String filenameIdentifier = "qDump";
	private static final int TIMEOUT = 2000; // 2 second timeout for synchronously receiving messages
	
	private int msgCountToDump = 0;
	private JmsLoginInfo loginInfo;
	private boolean initialized;
	private JmsDestination dstType;
	
	private JmsWriter jmsWriter;
	private InitialContext ctx;
	
	private String msgSelector;
	private String dstName;
	private String targetDirectory;

	private ConnectionFactory connFact;
	private String connFactName;
	private Connection connection;
	private Destination dst;
	private int msgsWritten = 0;
	private int msgsTraversed = 0;
	private boolean dumpComplete;
	
	// For durable subscriber dumps
	private String durableSubscriberName;
	private String clientId;
	private MessageCountUpdater messageCountUpdater;

	/**
	 * Main constructor the initialize the JMS Destination dumps
	 * @param ctx - Initial Context
	 * @param dstName - Destination name
	 * @param connFactName - Name of the connection factory
	 * @param msgSelector - Message selector
	 * @param loginInfo - Login Info
	 */
	public JmsDumpDestination(InitialContext ctx, String dstName, String connFactName, String msgSelector, JmsLoginInfo loginInfo) 
	{
		if(ctx == null || dstName == null || connFactName == null)
			throw new IllegalArgumentException("Unexpected null object passed to JmsDumpQueue constructor");
		this.ctx = ctx;
		this.dstName = dstName;
		this.msgSelector = msgSelector;
		this.connFactName = connFactName;
		this.loginInfo = loginInfo;
		this.initialized = false;
		this.dumpComplete = false;
	}
	
	public void setTargetDirectory(String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
	
	public boolean isDumpComplete() {
		return this.dumpComplete;
	}

	public void setMessageCountUpdater(MessageCountUpdater messageCountUpdater) {
		this.messageCountUpdater = messageCountUpdater;
	}
	/**
	 * Initialize JMS destination dump with anonymous authentication and no message selector
	 * @param ctx - Initial Context
	 * @param dstName - Destination name
	 * @param connFactName - Connection factory name
	 * @throws NamingException
	 * @throws JMSException
	 */
	public JmsDumpDestination(InitialContext ctx, String dstName, String connFactName)
	{
		this(ctx, dstName, connFactName, null, null);		
	}
	
	/**
	 * Initialize JMS destination dump with anonymous authentication and a message selector
	 * @param ctx
	 * @param dstName
	 * @param connFactName
	 * @param msgSelector
	 */
	public JmsDumpDestination(InitialContext ctx, String dstName, String connFactName, String msgSelector) 
	{
		this(ctx, dstName, connFactName, msgSelector, null);
	}

	/**
	 * Set only when you want your client to use a client Id. The value cannot be null.
	 * @param clientId
	 */
	public void setClientId(String clientId) {
		if(clientId == null) {
			LOG.info("Null client ID provided");
			throw new IllegalArgumentException("Null client ID provided");
		}
		this.clientId = clientId;
	}
	
	
	/**
	 * Set only when you want to dump a durable subscriber. Both clientID and Durable subscriber name must
	 * be set for the durable subscriber dump functionality to work. If not, an exception will be thrown
	 * during the initialization phased with init.
	 * @param durableSubscriberName
	 */
	public void setDurableSubscriberName(String durableSubscriberName) {
		if(durableSubscriberName == null) {
			LOG.info("Null durable subscriber name provided provided");
			throw new IllegalArgumentException("Null durable subscriber name provided");
		}
		this.durableSubscriberName = durableSubscriberName;
	}
	
	
	
	
	/**
	 * The init method initiates three types of processing
	 * 1. Queue Dump - Via Queue Browsers
	 * 2. Topic Dumps
	 * 3. Durable Subscriber Dumps
	 * 
	 * For a Topic, the algorithm to choose a destination when dst is a Topic instance 
	 * is as follows based on "clientId" and "durableSubscriberName" values 
	 * 
	 *<br/>
	 * -------------------------------------------------------------<br/>
	 * clientId | durableSubscriberName | LookFor					|<br/>
	 * -------------------------------------------------------------<br/>
	 * null		|	null				|	Topic					|<br/>
	 * null		|	value				|	Throw Error				|<br/>
	 * value	|	null				|	Topic					|<br/>
	 * value	|	value				|	Dump Durable Subscriber	|<br/>
	 * --------------------------------------------------------------<br/>
	 */
	
	public void init() throws JmsDiggerException {
		LOG.debug("Entering init method");
		try {
			dst = (Destination) ctx.lookup(dstName);
			
			if(dst == null)
				throw new IllegalArgumentException(dstName + " not found in JNDI");
			
			if(dst instanceof Queue) {
				dstType = JmsDestination.QUEUE;
				filenameIdentifier = "queueDump-" + dstName;
			}
			else {
				if(dst instanceof Topic) {
					if(durableSubscriberName == null) {
						dstType = JmsDestination.TOPIC;
						filenameIdentifier = "topicDump-" + dstName; 
					} else if(clientId != null) {
						dstType = JmsDestination.DURABLESUBSCRIBER;
						filenameIdentifier = "topicDurableSubscriberDump-" + dstName; 						
					} else {
						LOG.info("clientId cannot be null when durableSubscriberName has a value");
						throw new IllegalArgumentException("clientId cannot be null when durableSubscriberName is not null");
					}
				}
				else
					throw new IllegalArgumentException(dstName + " is neither a Queue nor a Topic");
			}
			
			connFact = (ConnectionFactory) ctx.lookup(this.connFactName);
			
			jmsWriter = new JmsWriter(targetDirectory, filenameIdentifier);
			if(loginInfo == null)
				connection = (Connection) connFact.createConnection();
			else
				connection = (Connection) connFact.createConnection(loginInfo.getUsername(), loginInfo.getPassword());
			
			// Set clientId - This is very important for durable subscriber identification
			if(clientId != null)
				connection.setClientID(clientId);
		} catch (JmsDiggerException e) {
			throw e;
		} catch (NamingException e) {
			LOG.info("Either a Queue or Destination name not found", e);
			throw JmsHelper.buildJmsDiggerException("Either a Queue or Destination name not found", e);
		} catch (JMSException e) {
			LOG.info("Could not create a connection", e);
			throw JmsHelper.buildJmsDiggerException("Could not create a connection", e);		
		}
		
		msgsWritten = 0;
		initialized = true;
		LOG.debug("Leaving init method");
		
	}
	
	/**
	 * Initializes the number of messages that objects of this class will 
	 * attempt to dump to the local filesystem. If value of count paramter is 
	 * less than 0, the value is left unchanged.
	 * @param count
	 */
	public void setMsgCountToDump(int count) {
		LOG.debug("Entering setMsgCountToDump method");
		if(count >= 0)
			msgCountToDump = count;
		LOG.debug("Leaving setMsgCountToDump method");
		
	}

	public int getMsgsWritten() {
		return msgsWritten;
	}
	
	/**
	 * Dumps contents of a Queue. Creates a QueueBrowser, gets the enumeration and writes messages.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JmsDiggerException
	 * @throws InterruptedException 
	 */
	private void initQueueDump() throws FileNotFoundException, IOException, JmsDiggerException, InterruptedException {
		LOG.debug("Entering initQueueDump method");
		
		QueueSession qSession = null;
		QueueBrowser qBrowser = null;
		Enumeration qEnum = null;
		jmsWriter.init();

		try {
			qSession = (QueueSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			if(msgSelector == null)
				qBrowser = qSession.createBrowser((Queue)dst);
			else
				qBrowser = qSession.createBrowser((Queue)dst, msgSelector);
			connection.start();
			boolean breakout = false;
			while(msgsTraversed <= msgCountToDump || msgCountToDump == 0) {
				qEnum = qBrowser.getEnumeration();
				if(!qEnum.hasMoreElements()) {
					Thread.sleep(2 * 1000); // Wait and try to get more elements
					continue; 
				}
				Message msg = null;
				while(qEnum.hasMoreElements()) {
					msg = (Message) qEnum.nextElement();
					try {
						jmsWriter.writeMsg(msg);
						msgsWritten++;
						
						/**
						 * Update the count in GUI
						 */
						if(messageCountUpdater != null) {
							messageCountUpdater.setCount(msgsWritten);
							Thread t = new Thread(messageCountUpdater);
							t.start();
						}
					} catch(JmsDiggerException ex) {
						//Swallow exceptions for failed write messages
					}
					msgsTraversed++;
					if(msgsTraversed >= msgCountToDump && msgCountToDump != 0) {
						breakout = true; // never break out when msgCountToDump is 0
						dumpComplete = true;
						break; // break from inner while loop
					}
				}
				if(breakout)
					break; // break from outer while loop
			}
		} catch (InvalidSelectorException ex) {
			LOG.info("Message selector exception encountered", ex);
			throw JmsHelper.buildJmsDiggerException("Message selector exception encountered", ex);
		} catch (JMSException ex) {
			LOG.info("A JMSException occured while creating a QueueBrowser", ex);
			throw JmsHelper.buildJmsDiggerException("A JMSException occured while creating a QueueBrowser", ex);
		} 
		

		
		try {
			qBrowser.close();
			qSession.close();
		} catch (JMSException e) {
			LOG.info("qBroswer or qSession close failed ", e);
			//Swallow the exception on close.
		}
		
		if(msgsTraversed > msgsWritten) {
			LOG.info("Messages Traversed " + msgsTraversed, ", Messages Written: " + msgsWritten);
			throw JmsHelper.buildJmsDiggerException("Not all messages were written. Check log for more details");
		}
		LOG.debug("Leaving initQueueDump method");
	}
	
	
	/**
	 * Write one message at a time. The object of this class is set as a message listener for Topics
	 *  Every message is received by this method is in turn written unless the number of messages 
	 *  written exceeds the target.
	 */
	public void onMessage(Message msg){
		LOG.debug("Entering onMessage method");
		if(msgCountToDump == 0 || msgsTraversed < msgCountToDump) {
			msgsTraversed++;
			try {
				jmsWriter.writeMsg(msg);
				msgsWritten++;
				/**
				 * Update the count in GUI
				 */
				if(messageCountUpdater != null) {
					messageCountUpdater.setCount(msgsWritten);
					Thread t = new Thread(messageCountUpdater);
					t.start();
				}
			} catch (JmsDiggerException e) {
				LOG.info("Message write failed", e);
			}
		} else {
			this.dumpComplete = true;
		}
		LOG.debug("Leaving onMessage method");
	}
	
	/**
	 * Dump contents of a topic. 
	 * @throws JmsDiggerException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void initTopicDump() throws JmsDiggerException, FileNotFoundException, IOException {
		LOG.debug("Entering initTopicDump method");
		TopicSession tSession;
		TopicSubscriber tSubscriber;
		jmsWriter.init();
		
		try {
			tSession = (TopicSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			tSubscriber = tSession.createSubscriber((Topic)dst);
			
			//Set the current object as message listener. All messages will be sent to onMessage method 
			tSubscriber.setMessageListener(this);
			connection.start(); // start the connection - very important
		} catch (JMSException e) {
			LOG.info("Failure while initiating a Topic Dump", e);
			throw JmsHelper.buildJmsDiggerException("Failure while initiating a Topic Dump", e);
		}
		
		//IMPORTANT: The subscriber and session must not be closed as closing them will cause the 
		// onMessage listener to be dysfunctional and no messages will be received.
		LOG.debug("Leaving initTopicDump method");
	}
	
	
	/**
	 * Dump contents of a durable subscriber. The reads are synchronous and block for duration specified 
	 * in the TIMEOUT (milliseconds) static variable. 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JmsDiggerException
	 */
	
	private void initDurableSubscriberDump() throws FileNotFoundException, IOException, JmsDiggerException {
		// Lot of duplicate code between initTopicDump and initDurableSubscriberDump.
		//TODO: Potentially combine these functions -- REVIEW and DECIDE.
		
		LOG.debug("Entering initDurableSubscriberDump method");
		Message msg;
		TopicSession tSession;
		TopicSubscriber tSubscriber;
		jmsWriter.init();
		
		try {
			tSession = (TopicSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			tSubscriber = tSession.createDurableSubscriber((Topic)dst, durableSubscriberName);
			connection.start();
			while(true) {
				msg = tSubscriber.receive(TIMEOUT); //Synchronous read, returns null when no message is received during TIMEOUT time
				
				//Ensure that null message is not passed to JmsWriter as it will throw an IllegalArgumentException
				if(msg == null) {
					continue;
				}
				if(msgCountToDump == 0 || msgsTraversed < msgCountToDump) {
					try {
						jmsWriter.writeMsg(msg);
						msgsWritten++;
						/**
						 * Update the count in GUI
						 */
						if(messageCountUpdater != null) {
							messageCountUpdater.setCount(msgsWritten);
							Thread t = new Thread(messageCountUpdater);
							t.start();
						}
					} catch (JmsDiggerException ex) {
						LOG.info("Message write failed ", ex);
						//Swallow single message write failure exception
					}
					
					msgsTraversed++;				
				} else {
					this.dumpComplete = true;
					break;
				}
			}
		} catch (JMSException e) {
			LOG.info("Failure while performing a Durable Subscriber Dump", e);
			throw JmsHelper.buildJmsDiggerException("Failure while performing a Durable Subscriber Dump", e);
		}
		
		try {
			tSubscriber.close();
			tSession.close();
		} catch (JMSException ex) {
			//Swallow the close exception
		}

		LOG.debug("Leaving initDurableSubscriberDump method");		
	}
	
	/**
	 * The public method that initiates the message dump from different types of destinations based on the 
	 * destination type determined by the init call.
	 * @throws IllegalAccessException
	 * @throws JmsDiggerException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	
	public void dump() throws IllegalAccessException, JmsDiggerException, FileNotFoundException, IOException, InterruptedException 
	{
		LOG.debug("Entering dump method");
		if(!initialized)
			throw new IllegalAccessException("dump method called without initializing");
		
		if(dstType == JmsDestination.QUEUE)
			initQueueDump();
		else {
			if(dstType == JmsDestination.TOPIC)
				initTopicDump();
			else {
				if(dstType == JmsDestination.DURABLESUBSCRIBER) 
					initDurableSubscriberDump();
				else {
					LOG.info(dstName + "is neither a Queue nor Topic or a Durable Subscriber");
					throw new IllegalArgumentException(dstName + "is neither a Queue nor Topic or a Durable Subscriber");
				}
			}
		}
		LOG.debug("Leaving dump method");
	}

	/**
	 * Important to call the close method to close all the file handles 
	 * for the jmsWriter and also close the JMS connection.
	 * @throws JMSException
	 * @throws IOException
	 */

	public void close() throws JMSException, IOException
	{
		LOG.debug("Entering close method");
		jmsWriter.close();
		connection.close();
		LOG.debug("Leaving close method");

	}

}
