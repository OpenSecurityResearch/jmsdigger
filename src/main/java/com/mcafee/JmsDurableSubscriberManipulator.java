package com.mcafee;

import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides capability to perform various operations related to durable subscribers. 
 * Given a Topic name, credentials (optional), durable subscriber name, the class can be used to perform the following actions: 
 * 1. Create random and named durable subscribers
 * 2. Erase durable subscribers
 * 3. Create large number of durable subscribers with random names
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsDurableSubscriberManipulator {
	private static final Logger LOG = LoggerFactory.getLogger(JmsDurableSubscriberManipulator.class);
	private InitialContext ctx;
	private Topic topic = null;
	private JmsLoginInfo loginInfo;
	private String cfName;
	private String topicName;
	private Connection connection; // Connection to the broker to last for the entire querying cycle
	private Session session; // Session created for the connection. This may or may not change between different request
	private boolean initialized = false;
	
	private void assertInitialization() {
		if(!initialized)
			throw new IllegalArgumentException(this.getClass().getName().toString() + "'s object is not initialized");
	}

	public JmsDurableSubscriberManipulator(InitialContext ctx, String topicName, String connFactName) {
		this(ctx, topicName, connFactName, null);

	}

	public JmsDurableSubscriberManipulator(InitialContext ctx, String topicName, String connFactName, JmsLoginInfo loginInfo) {
		this.ctx = ctx;
		this.loginInfo = loginInfo;
		this.cfName = connFactName;
		this.topicName = topicName;
	}
	
	public void init() throws JmsDiggerException {
		init(null);
	}

	/**
	 * Creates a connection, session and starts the connection. 
	 * clientId is set if provided. This is typically used for creating a durable subscriber.
	 * @param clientId
	 * @throws JmsDiggerException
	 */
	public void init(String clientId) throws JmsDiggerException {
		LOG.debug("Entering init");
		if(ctx == null || JmsHelper.isStringNullOrEmpty(cfName))
			throw JmsHelper.buildJmsDiggerException("Null value for InitialContext or Connection Factory Name");
		
		if(clientId == null)
			connection = JmsHelper.createConnection(ctx, cfName, loginInfo );
		else
			connection = JmsHelper.createConnection(ctx, cfName, loginInfo, clientId);
		
		try {
			topic = (Topic) ctx.lookup(topicName);
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();
		} catch (JMSException ex) {
			LOG.info("Error occured while initializing", ex);
			throw JmsHelper.buildJmsDiggerException("Error occured while initializing AMQStats", ex);
		} catch (NamingException ex) {
			LOG.info(topicName + " not found ", ex);
			throw JmsHelper.buildJmsDiggerException(topicName + " not found", ex);
		}
		
		initialized = true;
		LOG.debug("Leaving init");
	}
	
	/**
	 * Creates "count" number of subscribers for a Topic. Typically to be used during DoS
	 * by file system exhaustion. The attack may work like this:
	 * 1. Connect to a Topic
	 * 2. Create durable subscribers
	 * 3. Continue to Send large PERSISTENT messages with longer expiry time to the topic.
	 * 	
	 * @param count - number of TopicSubscribers to create
	 * @return ArrayList<TopicSubscriber> - An arraylist of all TopicSubscribers created
	 * @throws JmsDiggerException
	 */
	public ArrayList<TopicSubscriber> createDurableSubscribers(int count) throws JmsDiggerException {
		ArrayList<TopicSubscriber> aList = new ArrayList<TopicSubscriber>();
		if(count <= 0) 
			throw new IllegalArgumentException("Count cannot be negative");
		
		for(int i = 0; i < count; i++) {
			TopicSubscriber ts = createDurableSubscriber(JmsHelper.getRandomString());
			aList.add(ts);
		}
		return aList;
	}
	
	/**
	 * Creates a durable topic subscriber with a random name.
	 * @return TopicSubscriber
	 * @throws JmsDiggerException
	 */
	public TopicSubscriber createRandDurableSubscriber() throws JmsDiggerException {
		return createDurableSubscriber(JmsHelper.getRandomString());
	}
	
	/**
	 * Creates a durable subscriber with a specified name. 
	 * The durable subscriber created can be connected to later on to retrieve content. 
	 * @param name - Name of the topic subscriber
	 * @return TopicSubscriber - The topic subscriber object
	 * @throws JmsDiggerException
	 */
	
	public TopicSubscriber createDurableSubscriber(String name) throws JmsDiggerException {
		return createDurableSubscriber(name, "", false);
	}
	
	/**
	 * A Wrapper around createDurableSubscriber with three arguments. 
	 * The noLocal is set to false for every call.
	 * @param durableSubscriberName
	 * @param messageSelector
	 * @return
	 * @throws JmsDiggerException
	 */
	public TopicSubscriber createDurableSubscriber(String durableSubscriberName, String messageSelector) throws JmsDiggerException {
		return createDurableSubscriber(durableSubscriberName, messageSelector, false);
	}
	
	/**
	 * This call creates a Durable Subscriber for a topic. It is mandatory to initialize 
	 * with init before making this call. If not, the call will fail. 
	 * @param durableSubscriberName - Name of the durable subscriber
	 * @param messageSelector - The message selector. Can be null, blank or contain a valid value
	 * @param noLocal - Enable or disable messages generated from same host
	 * @return TopicSubscriber - Returns the topic subscriber created
	 * @throws JmsDiggerException - Throws for any errors
	 */
	public TopicSubscriber createDurableSubscriber(String durableSubscriberName, String messageSelector, boolean noLocal) throws JmsDiggerException  {
		LOG.debug("Entering createDurableSubscriber");
		assertInitialization();

		if(JmsHelper.isStringNullOrEmpty(durableSubscriberName))
			throw new IllegalArgumentException("Durable subscriber name cannot be null or blank");
		
		try {
			return session.createDurableSubscriber(topic, durableSubscriberName, messageSelector, noLocal);
		} catch (JMSException ex) {
			LOG.info("An error has occured while creating durable subscriber", ex);
			throw JmsHelper.buildJmsDiggerException("An error has occured while creating durable subscriber", ex);
		}
	}
	
	/**
	 * Erases a durable subscriber.
	 * @param durableSuscriberName
	 * @throws JmsDiggerException
	 */
	public void eraseDurableSubscriber(String durableSuscriberName) throws JmsDiggerException {
		LOG.debug("Entering eraseDurableSubscriber");
		assertInitialization();
		try {
			session.unsubscribe(durableSuscriberName);
		} catch (InvalidDestinationException ex) {
			LOG.info("No durable subscriber exists with name " + durableSuscriberName);
			throw JmsHelper.buildJmsDiggerException("No durable subscriber exists with name " + durableSuscriberName);
		} catch (JMSException ex) {
			LOG.info("Error occured while erasing a durable subscriber", ex);
			throw JmsHelper.buildJmsDiggerException("Error occured while erasing a durable subscriber", ex);
		}
		LOG.debug("Leaving eraseDurableSubscriber");
	}
	
	/**
	 * Close the JMS session and the connection
	 */
	public void close() {
		try {
				session.close();
				connection.close();
			} catch (JMSException e) {
				LOG.info("AMQOps done failed");
		}
	}
}
