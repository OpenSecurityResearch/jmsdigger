package com.mcafee;

import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Based on explanation and code example from ActiveMQ tutorials hosted at 
 * http://activemq.apache.org/statisticsplugin.html
 * @author Gursev Singh Kalra @ McAfee, Inc.
 */
public class AMQOps {
	private static final Logger LOG = LoggerFactory.getLogger(AMQOps.class);
	private static final int TIMEOUT = 1000; // Time to wait for the Queue or Topic status to return since the query is typically blocking.
	private InitialContext ctx;
	private JmsLoginInfo loginInfo;
	private String cfName;
	ArrayList<MapMessage> dstStats = new ArrayList<MapMessage>();
	private Connection connection; // Connection to the broker to last for the entire querying cycle
	private Session session; // Session created for the connection. This may or may not change between different request
	private Queue replyToQueue; // to connect status responses
	private Message message; // message to be sent and initialized during init call
	private MessageConsumer messageConsumer; // To consumer receive incoming messages containing stats
	private MessageProducer messageProducer; // To send messages to the destination
	private boolean statsGenerated = false;
	
	
	boolean initialized = false;

	public AMQOps(InitialContext ctx, String connFactName, JmsLoginInfo loginInfo) {
		this.ctx = ctx;
		this.loginInfo = loginInfo;
		this.cfName = connFactName;
	}
	
	public AMQOps(InitialContext ctx, String connFactName) {
		this(ctx, connFactName, null);
	}
	
	/**
	 * The init method initializes the object so it can be later used. It performs following actions<br/>
	 * 1. Creates a new connection and corresponding session <br/>
	 * 2. Creates a temporary queue to receive user response <br/>
	 * 3. Sets a message Consumer<br/>
	 * 4. Sets the message JMSReplyTo header to the temporary queue<br/>
	 * 5. Starts the connection and sets the initialized instance variable to true<br/>
	 * @throws JmsDiggerException
	 */
	public void init() throws JmsDiggerException  {
		LOG.debug("Entering init");
		if(ctx == null || JmsHelper.isStringNullOrEmpty(cfName))
			throw JmsHelper.buildJmsDiggerException("Null value for InitialContext or Connection Factory Name");
		
		connection = JmsHelper.createConnection(ctx, cfName, loginInfo);
		
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			replyToQueue = session.createTemporaryQueue();
			messageConsumer = session.createConsumer(replyToQueue);
			message = session.createMessage();
			message.setJMSReplyTo(replyToQueue);
			messageProducer = session.createProducer(null);
			connection.start();
		} catch (JMSException ex) {
			LOG.info("Error occured while initializing AMQStats", ex);
			throw JmsHelper.buildJmsDiggerException("Error occured while initializing AMQStats", ex);
		}
		
		initialized = true;
		LOG.debug("Leaving init");
	}
	
	/**
	 * This method returns the obtained statistics as String.
	 * @return String
	 * @throws JmsDiggerException
	 */
	public String getStatsAsString() throws JmsDiggerException {
		if(!initialized)
			throw new IllegalArgumentException("No stats available");
	
		if(!statsGenerated)
			throw new IllegalStateException("No stats available");
		
		StringBuilder sb = new StringBuilder();
		sb.append("");
		for(MapMessage mm : dstStats) {
			sb.append(JmsHelper.mapMessageToString(mm));
			sb.append("\n=================================\n\n");
		}
		return sb.toString();
	}

	/**
	 * Returns statistics of an ActiveMQ broker
	 * @return ArrayList<MapMessage>
	 * @throws JmsDiggerException
	 */
	public ArrayList<MapMessage> getBrokerStats() throws JmsDiggerException {
		return getDestinationStats("ActiveMQ.Statistics.Broker", false, true);
	}
	
	/**
	 * Returns statistics of a Subscription
	 * @return ArrayList
	 * @throws JmsDiggerException
	 */
	public ArrayList<MapMessage> getSubscriptionsStats() throws JmsDiggerException {
		return getDestinationStats("ActiveMQ.Statistics.Subscription", false, true);

	}
	
	/**
	 * Returns status of Queue with given name. It optionally allows 
	 * you to query Queue status with or without ActiveMQ.Statistics.Destination prefix to the name
	 * 
	 * @param qName Queue name
	 * @param prepend - Prepends ActiveMQ.Statistics.Destination. to the Queue name if true, else the qName is queried raw
	 * @return ArrayList
	 * @throws JmsDiggerException
	 */
	
	public ArrayList<MapMessage> getQueueStats(String qName, boolean prepend) throws JmsDiggerException {
		return getDestinationStats(qName, prepend, true);
	}
	
	/**
	 * Returns status of Topic with given name. It optionally allows 
	 * you to query Topic status with or without ActiveMQ.Statistics.Destination. 
	 * 
	 * @param tName - Topic name
	 * @param prepend - Prepends ActiveMQ.Statistics.Destination. to the topic name if true, else the tName is queried raw
	 * @return ArrayList
	 * @throws JmsDiggerException
	 */
	
	public ArrayList<MapMessage> getTopicStats(String tName, boolean prepend) throws JmsDiggerException {
		return getDestinationStats(tName, prepend, false);
	}
	
	/**
	 * This is the main (private) method to query the status and return to the caller.
	 * @param qName - Queue Name
	 * @param prepend - Boolean value that decides if ActiveMQ.Statistics.Destination. should be prepended to the qName
	 * @param isQueue - the destination is a Queue if true, else it is a Topic
	 * @return - ArrayList
	 * @throws JmsDiggerException
	 */
	private ArrayList<MapMessage> getDestinationStats(String qName, boolean prepend, boolean isQueue) throws JmsDiggerException {

		statsGenerated = false; // set to false before every attempt to get status
		if(qName == null)
			throw new IllegalArgumentException("null Destination name passed to getDestinationStats");

		assertInitialization();
		
		if(prepend)
			qName = "ActiveMQ.Statistics.Destination." + qName;

		dstStats = new ArrayList<MapMessage>();
		
		try {
			if(isQueue) {
				Queue queue = session.createQueue(qName);
				messageProducer.send(queue, message);
			} else {
				Topic topic = session.createTopic(qName);
				messageProducer.send(topic, message);
			}
			MapMessage stats;
			while((stats = (MapMessage) messageConsumer.receive(TIMEOUT)) != null) {
				dstStats.add(stats);
			}
			statsGenerated = true;
			return dstStats;
		} catch (JmsDiggerException ex) {
			throw ex;
		} catch (JMSException ex) {
			LOG.info("An error occured in getDestinationStats", ex);
			throw JmsHelper.buildJmsDiggerException("An error occured in getDestinationStats", ex);
		}
	}
	
	public void assertInitialization() {
		if(!initialized)
			throw new IllegalArgumentException(this.getClass().getName().toString() + "'s object is not initialized");
	}
	
	/**
	 * Main method to create a Queue or Topic on the target ActiveMQ broker
	 * @param dstName - Name of the destination
	 * @param isQueue - Boolean value indicating that the destination is a Queue (when true) or a Topic (when false)
	 * @return - Destination object
	 * @throws JmsDiggerException
	 */
	
	private Destination createDestination(String dstName, boolean isQueue) throws JmsDiggerException {
		if(JmsHelper.isStringNullOrEmpty(dstName))
			throw JmsHelper.buildJmsDiggerException("Invalid destination name parameter");
		
		assertInitialization();
		
		try {
			if(isQueue) {
				Queue q = session.createQueue(dstName);
				messageProducer.send(q, message);
				return q;
			} else {
				Topic t = session.createTopic(dstName);
				messageProducer.send(t, message);
				return t;
			}
		} catch (JMSException ex) {
			LOG.info("Destination " + dstName + " could not be created", ex);
			throw JmsHelper.buildJmsDiggerException("Destination " + dstName + " could not be created", ex);
		}
	}
	
	/**
	 * Creates a Queue on ActiveMQ broker
	 * @param queueName - Name of the queue
	 * @return - Queue object
	 * @throws JmsDiggerException
	 */
	
	public Queue createQueue(String queueName) throws JmsDiggerException {
		return (Queue) createDestination(queueName, true);
	}
	
	/**
	 * Creates a Topic on ActiveMQ broker
	 * @param topicName - Name of the topic
	 * @return - Topic object
	 * @throws JmsDiggerException
	 */
	public Topic createTopic(String topicName) throws JmsDiggerException {
		return (Topic) createDestination(topicName, false);
	}
	
	public void close() {
		try {
				messageProducer.close();
				messageConsumer.close();
				session.close();
				connection.close();
			} catch (JMSException e) {
				LOG.info("AMQOps done failed");
		}

	}

}
