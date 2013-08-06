package com.mcafee;

import static org.junit.Assert.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Gursev Singh Kalra  @ McAfee, Inc.
 *
 */
public class JmsInitialContextFactoryTest {
	private JmsInitialContextFactory contextFactory;
			
	@Before
	public void setUp() throws Exception {
		//TODO: right now this is hard tied to ActiveMQ. But will be modified to 
		//run generic tests when the tool adds support for multiple providers
		contextFactory = new JmsInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://localhost:61616");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJmsInitialContextFactoryExists() throws JmsDiggerException {
		// make sure that ActiveMQ instance is running
		InitialContext ctx = contextFactory.getInitialContext();
		assertNotNull(ctx);
	}

	@Test (expected=JmsDiggerException.class)
	public void testJmsInitialContextFactoryDoesNotExist() throws JmsDiggerException {
		contextFactory = new JmsInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactoryyyyy", "tcp://localhost:61616");
		contextFactory.getInitialContext();
	}

	@Test
	public void testAddQueueWhenEditableIsFalse() throws JmsDiggerException {
		InitialContext ctx = contextFactory.getInitialContext();
		contextFactory.addQueue("bangbang", "bang");
		try {
			assertFalse(ctx.getEnvironment().containsKey("bangbang"));
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRawPropertyToEnv() throws JmsDiggerException {
		contextFactory.addRawPropertyToEnv("bangbang", "bang");
		InitialContext ctx = contextFactory.getInitialContext();
		try {
			assertNotNull(ctx.getEnvironment().containsKey("bangbang"));
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

}
