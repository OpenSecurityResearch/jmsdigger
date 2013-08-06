package com.mcafee;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Gursev Singh Kalra  @ McAfee, Inc.
 *
 */
public class JmsDurableSubscriberManipulatorTest {
	private InitialContext ctx;	
	private String durableSubscriberTopic = "durableSubscriberTopic";
	private JmsDurableSubscriberManipulator manipulator = null;
	private String connFactName = "ConnectionFactory";
	TopicSubscriber tsub;
	private String durableSubscriberName = "durableSubscriberName";
	private String clientId = "gursev";
	ArrayList<TopicSubscriber> tsAlist;
	

	@Before
	public void setUp() throws Exception {
		JmsInitialContextFactory contextFactory = new JmsInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://localhost:61616");
		contextFactory.addConnectionFactory(connFactName);
		contextFactory.addTopic(durableSubscriberTopic, "jms." + durableSubscriberTopic);
		ctx = contextFactory.getInitialContext();
		manipulator = new JmsDurableSubscriberManipulator(ctx, durableSubscriberTopic, connFactName);
		manipulator.init(clientId);
	}

	@After
	public void tearDown() throws Exception {
		manipulator.close();
	}

	@Test
	public void testCreateDurableSubscribers() throws JmsDiggerException {
		int count = 1;
		tsAlist = new ArrayList<TopicSubscriber>();
		tsAlist = manipulator.createDurableSubscribers(count);
		assertEquals(count, tsAlist.size());
	}

	@Test
	public void testCreateRandDurableSubscriber() throws JmsDiggerException, JMSException {
		tsub = null;
		tsub = manipulator.createRandDurableSubscriber();
		assertNotNull(tsub);
	}

	@Test
	public void testCreateDurableSubscriberWithName() throws JmsDiggerException {
		tsub = null;
		tsub = manipulator.createDurableSubscriber(durableSubscriberName);
		assertNotNull(tsub);
	}

	@Test
	public void testCreateDurableSubscriberWithNameMessageSelectorAndNoLocal() throws JmsDiggerException {
		tsub = null;
		tsub = manipulator.createDurableSubscriber(durableSubscriberName, "test", true);
		assertNotNull(tsub);
	}
	
	@Test
	public void testEraseDurableSubscriber() throws JmsDiggerException {
		manipulator.createDurableSubscriber(durableSubscriberName+"Eraseme", "test", true);
		manipulator.close();
		manipulator.init(clientId);
		manipulator.eraseDurableSubscriber(durableSubscriberName+"Eraseme");
	}

}
