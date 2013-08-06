package com.mcafee;

import javax.jms.JMSException;

/**
 * This is the main exception class for the JMSDigger Tool
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsDiggerException extends JMSException {

	private static final long serialVersionUID = 1L;

	public JmsDiggerException(String msg) {
		super(msg);
	}
	
	public JmsDiggerException(String msg, String errCode) {
		super(msg, errCode);
	}
	

}
