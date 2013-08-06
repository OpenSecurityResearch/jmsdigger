package com.mcafee;

import static org.junit.Assert.*;

import org.apache.activemq.util.IdGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Gursev Singh Kalra  @ McAfee, Inc.
 *
 */
public class IdGeneratorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		for(int i = 0; i < 100; i++)
			System.out.println(new IdGenerator().generateId());
	}

	@Test
	public void test() {
		
		fail("Not yet implemented");
	}

}
