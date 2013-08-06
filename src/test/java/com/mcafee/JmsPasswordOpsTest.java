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
public class JmsPasswordOpsTest {
	private JmsPasswordOps passwdOps = new JmsPasswordOps();
	
	@Before
	public void setUp() throws Exception {


		passwdOps.addPassword("new");
		passwdOps.addPassword("new1");
		passwdOps.addPassword("new2");
		passwdOps.addPassword("new3");
		passwdOps.addPassword("new4");
		passwdOps.addPassword("new5");
		passwdOps.addPassword("new6");
		passwdOps.addPassword("new7");
		passwdOps.addPassword("activemq");
		passwdOps.addPassword("new8");
		passwdOps.addPassword("new9");
		passwdOps.addPassword(" ");
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testDecrypt()  {
		String pass;
		pass = passwdOps.decrypt("Cf3Jf3tM+UrSOoaKU50od5CuBa8rxjoL");
		assertEquals("password", pass);
		pass = passwdOps.decrypt("mYRkg+4Q4hua1kvpCCI2hg==");
		assertEquals("manager", pass);

	}
	
	@Test 
	public void testDecryptInvalidInput() {
		assertNull(passwdOps.decrypt("mYRkg+4Q4hua1kvpCCI2hg="));

	}

}
