package com.mcafee;

import static org.junit.Assert.*;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsHelperTest {

	private JmsInitialContextFactory contextFactory;
	private InitialContext ctx;

	@Before
	public void setUp() throws Exception {
		contextFactory = new JmsInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://localhost:61616");
		contextFactory.addTopic("exploits", "jms.exploits");
		contextFactory.addQueue("notInJndi", "jms.notInJndi");
		ctx = contextFactory.getInitialContext();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDestinationTrueInitCtxAndTrueJNDI() throws JmsDiggerException {
		Destination dest = JmsHelper.getDestination(ctx, "exploits");
		assertNotNull(dest);
	}
	
//	@Test
//	public void randomTest() throws NamingException
//	{
//		InitialContext ctx = new InitialContext();
//		//QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory)ctx.lookup("ConnectionFactory");
//		Queue queue = (Queue)ctx.lookup("submissions");
//		assertNull(queue);
//		
//	}

	
	/**
	 * ActiveMQ auto creates new destinations during context looks up for a destination that is not present on the Broker
	 */
	@Test
	public void testGetDestinationTrueInitCtxAndFalseJNDI() throws JMSException{
		Destination dest = JmsHelper.getDestination(ctx, "notInJndi");
		if(dest instanceof Queue)
		{
			System.out.println(((Queue) dest).getQueueName());
		}
		if(dest instanceof Topic)
		{
			System.out.println(((Topic) dest).getTopicName());
		}
		
		assertNotNull(dest);
	}

	
	@Test (expected=JmsDiggerException.class)
	public void testGetDestinationFalseInitCtxAndTrueJNDI() throws JmsDiggerException {
		JmsHelper.getDestination(ctx, "results");
	}

	
	@Test (expected=JmsDiggerException.class)
	public void testGetDestinationFalseInitCtxAndFalseJNDI() throws JmsDiggerException {
		JmsHelper.getDestination(ctx, "nowhere");
	}

	
	@Test
	public void testGetConnectionFactory() {
		
	}
	
	@Test
	public void testGetDestinationIntrusive()
	{
		String cfName = "ConnectionFactory";
		String destName = "exploits";
		//Destination dest = JmsHelper.getDestinationIntrusiveConsumer(ctx, cfName, null, destName, result);//(ctx, cfName, destName, result);
		//Destination dest = JmsHelper.getDestinationIntrusiveCreateDestination(ctx, cfName, null, destName, result);//(ctx, cfName, destName, result);
		//assertNotNull(dest);
		//loginInfo = new JmsLoginInfo("gursev", "kalra");
	}

}
