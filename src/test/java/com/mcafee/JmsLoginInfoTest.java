package com.mcafee;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsLoginInfoTest {
	
	private JmsLoginInfo loginInfo = null; 
	@Before
	public void setUp() throws Exception {
		loginInfo = new JmsLoginInfo("gursev", "kalra");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetUsername() {
		assertEquals("gursev", loginInfo.getUsername());
	}

	@Test
	public void testGetUsernameWrong() {
		assertNotEquals("_gursev", loginInfo.getUsername());
	}
	
	@Test
	public void testGetPassword() {
		assertEquals("kalra", loginInfo.getPassword());
	}

}
