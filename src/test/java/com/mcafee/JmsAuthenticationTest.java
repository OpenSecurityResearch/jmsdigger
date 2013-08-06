package com.mcafee;

import static org.junit.Assert.*;

import javax.naming.InitialContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsAuthenticationTest {
	private JmsAuthentication jmsAuthn;
	private JmsInitialContextFactory contextFactory;
	private InitialContext ctx;
	private String connFact = "ConnectionFactory";
	private JmsLoginInfo loginInfo;
	private boolean state;
	private boolean testingAnonymous; 
	

	@Before
	public void setUp() throws JmsDiggerException  {
		contextFactory = new JmsInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://192.168.10.101:61626");
		contextFactory.addConnectionFactory(connFact);
		ctx = contextFactory.getInitialContext();
		jmsAuthn = new JmsAuthentication(ctx, connFact);
		// Make sure to change Messaging broker running configuration before changing this variable. 
		testingAnonymous = true;
	}

	@After
	public void tearDown() throws Exception {
		
	}

	/**
	 * only one of testIsLoginInfoValid 
	 * @throws JmsDiggerException 
	 */
	@Test
	public void testValidUsernameInvalidPassword() throws JmsDiggerException {
		loginInfo = new JmsLoginInfo("system", "iNvAlIdPassword!");
		state = jmsAuthn.isLoginInfoValid(loginInfo);
		if(testingAnonymous)
			assertEquals(true, state);
		else
			assertEquals(false, state);
	}
	
	/**
	 * only one of testIsLoginInfoValid 
	 * @throws JmsDiggerException 
	 */
	@Test
	public void testInvalidUsernameValidPassword() throws JmsDiggerException {
		loginInfo = new JmsLoginInfo("iNvAlId", "manager");
		state = jmsAuthn.isLoginInfoValid(loginInfo);
		if(testingAnonymous)
			assertEquals(true, state);
		else
			assertEquals(false, state);
	}
	
	/**
	 * only one of testIsLoginInfoValid 
	 * @throws JmsDiggerException 
	 */
	@Test
	public void testSystemUsernameManagerPassword() throws JmsDiggerException {
		loginInfo = new JmsLoginInfo("\"", "pas");
		state = jmsAuthn.isLoginInfoValid(loginInfo);
		if(!testingAnonymous)
			assertTrue(state);
	}

	@Test
	public void testIsAnonymousAuthAlowed() throws JmsDiggerException {
		state = jmsAuthn.isAnonymousAuthAlowed();
		if(testingAnonymous)
			assertEquals(true, state);
		else
			assertEquals(false, state);
	}
	
	@Test 
	public void testNullUsernameTest() throws JmsDiggerException {
		loginInfo = new JmsLoginInfo(null, "manager");
		state = jmsAuthn.isLoginInfoValid(loginInfo);
		if(testingAnonymous)
			assertTrue(state);
		else
			assertFalse(state);
	}

	@Test 
	public void testNullPasswordTest() throws JmsDiggerException {
		loginInfo = new JmsLoginInfo("system", null);
		state = jmsAuthn.isLoginInfoValid(loginInfo);
		if(testingAnonymous)
			assertTrue(state);
		else
			assertFalse(state);
		
	}

	@Test 
	public void testNullUsernameAndNullPasswordTest() throws JmsDiggerException {
		loginInfo = new JmsLoginInfo(null, null);
		state = jmsAuthn.isLoginInfoValid(loginInfo);
		if(testingAnonymous)
			assertTrue(state);
		else
			assertFalse(state);
	}

}
