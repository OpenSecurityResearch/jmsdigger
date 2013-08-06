package com.mcafee;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.Topic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class AMQOpsTest {

	private AMQOps opsTest;
	@Before
	public void setUp() throws Exception {
		opsTest = new AMQOps(JmsHelper.getActiveMQInitialContext(), "ConnectionFactory");
		opsTest.init();
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void brokerStatusAndSubscriptionStatusTest() throws JmsDiggerException  {
		ArrayList<MapMessage> status = null;
		int expected = 1;
		status = opsTest.getBrokerStats();
		System.out.println(JmsHelper.mapMessageToString(status.get(0), "================"));
		
		assertNotNull(status);
		assertEquals(expected, status.size());
		
		status = null;
		status = opsTest.getSubscriptionsStats();
		assertNotNull(status);
		assertEquals(expected, status.size());
		
	}
	
	@Test
	public void amqstatsAndamqstatstTest() throws JmsDiggerException {
		String dName = null;
		ArrayList<MapMessage >status = null;
		int expected = 1;
		
		dName = "jms.amqQueueStats";
		status = opsTest.getQueueStats(dName, true);
		assertNotNull(status);
		assertEquals(expected, status.size());
		
		status = null;
		
		dName = "jms.amqTopicStats";
		status = opsTest.getTopicStats(dName, true);

		assertNotNull(status);
		assertEquals(expected, status.size());
		
		status = null;
		expected = 0 ;
		
		dName = "aa";
		status = opsTest.getTopicStats(dName, true);
		assertNotNull(status);
		assertEquals(0, status.size());
	}
	
	@Test
	public void amqWildCardStatsTest() throws JmsDiggerException {
		String dName = null;
		ArrayList<MapMessage> status = null;
		dName = "jms.*";
		status = opsTest.getQueueStats(dName, true);

		assertNotNull(status);
		assertTrue(status.size() > 1);
		
		dName = "jms.*";
		status = opsTest.getTopicStats(dName, true);
		assertNotNull(status);
		assertTrue(status.size() > 1);	
	}
	
	@Test 
	public void createQueueAndDestinationTest() throws JmsDiggerException {
		ArrayList<MapMessage> aList;
		String dstName = "lsadf2342";
		
		Queue q = opsTest.createQueue(dstName);
		int expected = 1;
		
		aList = opsTest.getQueueStats(dstName, true);
		assertNotNull(aList);
		assertEquals(expected, aList.size());

		Topic t = opsTest.createTopic(dstName);		
		aList = opsTest.getTopicStats(dstName, true);
		assertNotNull(aList);
		assertEquals(expected, aList.size());

	}

}
