package com.mcafee;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;
//import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a helper class that offers various functionalities:<br/>
 * 1. Creates random strings with <b> getRandomString </b> method <br/>
 * 2. Obtain a destination object with <b>getDestination</b> <br/>
 * 3. Converts byte array to hex String with <b> byteArrayToHexString </b><br/>
 * 4. Converts a MapMessage to string with <b>mapMessageToString</b> method<br/>
 * 5. Creates a JMS connection and returns the connection object with <b>createConnection</b> method<br/>
 * 6. Breaks a string to character array with <b>stringToCharArrayString</b><br/>
 * 7. Build JmsDiggerException with two overloaded methods with name <b>buildJmsDiggerException</b> <br/>
 * 8. Creates a connection factory from initial context and connection factory name with <b>getConnectionFactory</b> method<br/>
 * 9. Creates ActiveMQ's initial context with <b>getActiveMQInitialContext</b> method <br/> 
 * @author Gursev Singh Kalra  @ McAfee, Inc.
 *
 */
public class JmsHelper {
	
	private static final Logger LOG = LoggerFactory.getLogger(JmsHelper.class);
	private static Random random = new Random(); 
	
	/**
	 * Generates and returns a random string
	 * @return String
	 */
	public static String getRandomString() {
		return new BigInteger(64, random).toString(16);
	}
	
	/**
	 * Converts exception's stack trace to a string for easy printing and consumption
	 * @param ex
	 * @return
	 */
	public static String exceptionStacktraceToString(Exception ex) {
		StringWriter sWriter = new StringWriter();
		PrintWriter pWriter = new PrintWriter(sWriter);
		ex.printStackTrace(pWriter);
		String result = sWriter.toString();
		pWriter.close();
		try {
			sWriter.close();
		} catch (IOException e) {
			// Eating it up
		}
		return result;
	}

	/**
	 * Obtains a Destination object. Any generated exception is added to the cause.
	 * @param ctx - InitialContext
	 * @param destName - Destination Name
	 * @return - Destination Object
	 * @throws JmsDiggerException
	 */
	public static Destination getDestination(InitialContext ctx, String destName) throws JmsDiggerException
	{
		LOG.debug("Entering getDestination method");
		Destination dest = null;
		if(ctx == null || isStringNullOrEmpty(destName))
		{
			LOG.info("Either InitialContext or Destination Name is null");
			throw new JmsDiggerException("Either InitialContext or Destination Name is null");
		}

		try
		{
			dest = (Destination)(ctx.lookup(destName));
		}
		catch(ClassCastException ex)
		{
			LOG.info("The returned object for name " + destName + " was not of type Destination", ex);
			throw buildJmsDiggerException("The returned object for name " + destName + " was not of type Destination", ex);
		}
		catch(NamingException ne)
		{
			LOG.info("No destination found with name " + destName, ne);
			throw buildJmsDiggerException("No destination found with name " + destName, ne);
		}
		
		LOG.debug("Leaving getDestination method");
		return dest;
		
	}
	
	/**
	 * Convert a byte array to hex string 
	 * Converts a byte array to string representation of hex digits
	 * Example conversion -> { 0x12, 0x23, 0x32, 0xA5 }
	 * @param b
	 * @return
	 */
	public static String byteArrayToHexString(byte[] b) {

		StringBuilder sb = new StringBuilder();
		if (b.length == 0)
			return "{ }";

		String str = Hex.encodeHexString(b);
		String[] twoCharArray = str.split("(?<=\\G.{2})");

		sb.append("{ ");
		for (String s : twoCharArray)
			sb.append("0x" + s + ", ");

		sb.deleteCharAt(sb.length() - 2);
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * Convers a map message to a string with a custom header
	 * @param msg - The MapMessage
	 * @return string representation of the message
	 * @throws JmsDiggerException
	 */
	public static String mapMessageToString(MapMessage msg) throws JmsDiggerException {
		return mapMessageToString(msg, null);
	}
	
	/**
	 * Convers a map message to a string with a custom header
	 * @param msg - The MapMessage
	 * @param customHdr - The header to be used for separating message
	 * @return string representation of the message
	 * @throws JmsDiggerException
	 */
	public static String mapMessageToString(MapMessage msg, String customHdr) throws JmsDiggerException {
		Enumeration e = null;
		StringBuilder sb = new StringBuilder();
		String name;
		Object value;
		
		try {
			e = ((MapMessage) msg).getMapNames();
			
			if (e.hasMoreElements()) {
				if(customHdr != null)
					sb.append(customHdr + "\n");
				
				while (e.hasMoreElements()) {
					name = e.nextElement().toString();
					sb.append("\t" + name + " : ");
					value = msg.getObject(name);
					if (value instanceof byte[])
						sb.append(JmsHelper.byteArrayToHexString((byte[]) value) + "\n");
					else
						sb.append(value + "\n");
				}
			}
		} catch(JMSException ex) {
			throw buildJmsDiggerException("An exception occured while creating String representation of MapMessage");
		}
		return sb.toString();
	}
	
	
	/**
	 * This method returns a connection or null if connection could not be generated.
	 * @param ctx - Initial Context
	 * @param cfName - ConnectionFactory name
	 * @param loginInfo - JmsLoginInfo object with username and password
	 * @param clientId - Client ID to be used
	 * @return
	 * @throws JmsDiggerException 
	 */
	public static Connection createConnection(InitialContext ctx, String cfName, JmsLoginInfo loginInfo, String clientId) throws JmsDiggerException {
		LOG.debug("Entering getConnection method");
		Connection conn = null;
		ConnectionFactory connFact = getConnectionFactory(ctx, cfName);

		try
		{
			if(loginInfo == null)
				conn = connFact.createConnection();
			else
				conn = connFact.createConnection(loginInfo.getUsername(), loginInfo.getPassword());
			//conn.setClientID(getRandomString());
			if(clientId != null)
				conn.setClientID(clientId);
			// A new session is created as ActiveMQ does not initiate a new connection unless session creation is attempted. 
			// The connection is valid and usable only if it allows session creation.
			Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			//Close the session if it gets created as it is not needed.
			if(sess != null) 
				sess.close();
		}
		catch(JMSException e)
		{
			LOG.info("Connection to the broker failed", e);
			throw buildJmsDiggerException("Connection to the broker failed", e);
		}
		
		LOG.debug("Leaving getConnection method");
		return conn;		
	}
	
	
	/**
	 * Convert a string to its character representation
	 * @param str
	 * @return
	 */
	public static String stringToCharArrayString(String str) {
		StringBuilder sb = new StringBuilder();
		if(str == null) 
			throw new IllegalArgumentException("String cannot be null");
		if(isStringNullOrEmpty(str))
			return "";
		
		sb.append("{");
		char[] c = str.toCharArray();
		for(char chr : c) {
			sb.append("\""+chr+"\"");
			sb.append("=> ");
			sb.append((int)chr);
			sb.append(", ");
		}
		sb.append("}");
		return sb.toString();
		
	}
	
	public static Connection createConnection(InitialContext ctx, String cfName, JmsLoginInfo loginInfo) throws JmsDiggerException {
		return createConnection(ctx, cfName, loginInfo, null);
	}
	
	/**
	 * This belongs to Unit tests. TODO move it to unit test code
	 * @return
	 * @throws JmsDiggerException
	 */
	public static InitialContext getActiveMQInitialContextForUnitTest() throws JmsDiggerException
	{
		JmsInitialContextFactory contextFactory = new JmsInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "vm://localhost?broker.persistent=false");
		contextFactory.addConnectionFactory("ConnectionFactory");
		contextFactory.addQueue("submissions", "jms.submissions");
		InitialContext ctx = contextFactory.getInitialContext();
		return ctx;
	}
	
	/**
	 * Returns InitialContext for ActiveMQ
	 * @return InitialContext
	 * @throws JmsDiggerException
	 */
	public static InitialContext getActiveMQInitialContext() throws JmsDiggerException
	{
		JmsInitialContextFactory contextFactory = new JmsInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://192.168.127.130:61616");
		contextFactory.addConnectionFactory("ConnectionFactory");
		contextFactory.addQueue("submissions", "jms.submissions");
		InitialContext ctx = contextFactory.getInitialContext();
		return ctx;
	}
	
	
	/**
	 * This method returns connection created with anonymous authentication.
	 * @param ctx - Initial Context
	 * @param cfName - ConnectionFactory name
	 * @param result - JmsResposne object, primarily a DAO to carry back details on failure.
	 * @return
	 * @throws JmsDiggerException 
	 */
	public static Connection createConnection(InitialContext ctx, String cfName) throws JmsDiggerException
	{
		return createConnection(ctx, cfName, null);
	}
	
	/**
	 * Build a JMSDiggerException when another exception has occured and we want to wrap
	 * that exception with JMSDiggerException
	 * @param msg - The message
	 * @param cause - Cause exception
	 * @return
	 */
	public static JmsDiggerException buildJmsDiggerException(String msg, Throwable cause) {
		JmsDiggerException je = new JmsDiggerException(msg);
		je.initCause(cause);
		return je;
		
	}

	/**
	 * Build a JMSDiggerException when another exception has occured and we want to wrap
	 * that exception with JMSDiggerException
	 * @param msg - The message
	 * @return
	 */
	public static JmsDiggerException buildJmsDiggerException(String msg) {
		JmsDiggerException je = new JmsDiggerException(msg);
		return je;
	}
	
	/**
	 * This method returns a connection factory from initial context and connection factory name
	 * @param ctx - Initial Context
	 * @param cfName - Connection factory name
	 * @return - ConnectionFactory
	 * @throws JmsDiggerException
	 */
	public static ConnectionFactory getConnectionFactory(InitialContext ctx, String cfName) throws JmsDiggerException
	{
		LOG.debug("Entering getConnectionFactory method");
		if(ctx == null)
		{
			throw new JmsDiggerException("InitialContext parameter was null");
		}
		
		//Doing this because of the way ActiveMQ handles local resolution of Connection Factory names
		if(isStringNullOrEmpty(cfName))
		{
			cfName = "ConnectionFactory";
		}
		
		ConnectionFactory cf = null;
		try
		{
			cf = (ConnectionFactory)(ctx.lookup(cfName));
		}
		catch(ClassCastException ex)
		{
			LOG.info(cfName + " is not of type ConnectionFactory", ex);
			//The returned object can be of type Destination
			throw buildJmsDiggerException(cfName + " is not of type ConnectionFactory", ex);
		}
		catch(NamingException ne)
		{
			LOG.info("No Connection Factory with name " + cfName + " identified", ne);
			throw buildJmsDiggerException("No Connection Factory with name " + cfName + " identified", ne);
		}

		LOG.debug("Leaving getConnectionFactory method");
		return cf;
	}
	
	/**
	 * String value is checked against null or with spaces 
	 * @param str
	 * @return true or false
	 */
	public static boolean isStringNullOrEmpty(String str)
	{
		if(str == null || str.trim().equals(""))
			return true;
		return false;
	}
}
