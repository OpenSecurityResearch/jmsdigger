package com.mcafee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class FuzzAMQOps {
	private static Logger LOG = LoggerFactory.getLogger(FuzzAMQOps.class);

	private AMQOps opsTest;
	private static BrokerService broker;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		broker = new BrokerService();
		broker.setPersistent(false);
		broker.start();		
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		broker.stop();
	}
	
	@Before
	public void setUp() throws Exception {
		//opsTest = new AMQOps(JmsHelper.getActiveMQInitialContextForUnitTest(), "ConnectionFactory");
		opsTest = new AMQOps(JmsHelper.getActiveMQInitialContext(), "ConnectionFactory");
		opsTest.init();
	}

	@After
	public void tearDown() throws Exception {

	}

//	@Test
//	public void brokerStatusAndSubscriptionStatusTest() throws JmsDiggerException  {
//		ArrayList<MapMessage> status = null;
//		int expected = 1;
//		status = opsTest.getBrokerStats();
//		assertNotNull(status);
//		assertEquals(expected, status.size());
//		
//		status = null;
//		status = opsTest.getSubscriptionsStats();
//		assertNotNull(status);
//		assertEquals(expected, status.size());
//		
//	}
//	
	@Test
	public void amqstatsAndamqstatstTest() throws JmsDiggerException {
		
		JmsMessageContentGenerator gen = new JmsMessageContentGenerator();
		gen.setMaxValue((int)256);
		String dstName = null;
		ArrayList<MapMessage>status;
		Topic q;
		char[] c;
		
		for(int i = 1 ; i < 10; i+=1) {
			try {
				System.out.println(i);
				dstName = gen.getString(100);
				LOG.info("Destination Name => " + dstName);
				LOG.info(JmsHelper.stringToCharArrayString(dstName));
				status = opsTest.getQueueStats(dstName, false);
				if(status != null && status.size() > 0)
					for(MapMessage m: status) 
						System.out.println(JmsHelper.mapMessageToString(m));
				else
					System.out.println("Nothing returned for " + dstName);
			} catch (JmsDiggerException ex) {
				LOG.info("Error occured at i => " + i, ex);
			} catch (Throwable ex) {
				LOG.info("This caused error => " + dstName);
				LOG.info("Error occured at i => " + i, ex);
			}
			
		}


//		ArrayList<MapMessage >status = null;
//		int expected = 1;
//		
//		dstName = "jms.amqQueueStats";
//		status = opsTest.getQueueStats(dstName, true);
//		assertNotNull(status);
//		assertEquals(expected, status.size());
//		
//		status = null;
//		
//		dstName = "jms.amqTopicStats";
//		status = opsTest.getTopicStats(dstName, true);
//
//		assertNotNull(status);
//		assertEquals(expected, status.size());
//		
//		status = null;
//		expected = 0 ;
//		
//		dstName = "aa";
//		status = opsTest.getTopicStats(dstName, true);
//		assertNotNull(status);
//		assertEquals(0, status.size());
	}
//	
//	@Test
//	public void amqWildCardStatsTest() throws JmsDiggerException {
//		String dName = null;
//		ArrayList<MapMessage> status = null;
//		dName = "jms.*";
//		status = opsTest.getQueueStats(dName, true);
//
//		assertNotNull(status);
//		assertTrue(status.size() > 1);
//		
//		dName = "jms.*";
//		status = opsTest.getTopicStats(dName, true);
//		assertNotNull(status);
//		assertTrue(status.size() > 1);	
//	}
	
	//@Test
	public void createLongQueueNames() throws JmsDiggerException, Exception {

		//tried all 65536 characters. None returned error
		JmsMessageContentGenerator gen = new JmsMessageContentGenerator();
		gen.setMaxValue((int)256);
		String dstName = "";
		Topic q;
		char[] c;
		
		for(int i = 1 ; i < 5000; i+=1) {
			try {
				System.out.println(i);
				dstName = gen.getString(5);
				dstName = "//*/" + dstName;
				LOG.info("Destination Name => " + dstName);
				LOG.info(JmsHelper.stringToCharArrayString(dstName));
				q = opsTest.createTopic(dstName);
			} catch (JmsDiggerException ex) {
				LOG.info("Error occured at i => " + i, ex);
			} catch (Throwable ex) {
				LOG.info("This caused error => " + dstName);
				LOG.info("Error occured at i => " + i, ex);
			}
			
		}
	}
	
	//@Test
	public void testObjectName() throws MalformedObjectNameException, NullPointerException {
		ObjectName onm = new ObjectName("aaaaaaaaaaaaa.ddddd");
		System.out.println(onm.toString());
	}
	
	//@Test
	public void createQueueAndDestinationTest() throws JmsDiggerException {
		//tried all 65536 characters. None returned error
		ArrayList<MapMessage> aList;
		String dstName;
		Queue q;
		
		for(int i = 55000 ; i < 65536; i ++) {
			try {
				System.out.println(i);
				dstName = "" + (char)i;
				q = opsTest.createQueue(dstName);
			} catch (JmsDiggerException ex) {
				LOG.info("Error occured at i => " + i, ex);
			}
			
		}
//		
//		aList = opsTest.getQueueStats(dstName, true);
//		assertNotNull(aList);
//		assertEquals(expected, aList.size());
//
//		Topic t = opsTest.createTopic(dstName);		
//		aList = opsTest.getTopicStats(dstName, true);
//		assertNotNull(aList);
//		assertEquals(expected, aList.size());

	}

}
