package com.mcafee;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.util.IdGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.lang.RandomStringUtils;

class RandomPerson implements Serializable {
	private static Random random = new Random(); 
	private String name;
	private int age;
	public RandomPerson() {
		name = RandomStringUtils.random(random.nextInt(15));
		age = random.nextInt(20);
	}
}

/**
 * 
 * @author Gursev Singh Kalra  @ McAfee, Inc.
 *
 */
public class JmsDumpDestinationTest {
	private InitialContext ctx;
	
	private String dumpQueue11Msgs = "dumpQueue11Msgs";
	private String dumpQueue00Msgs = "dumpQueue00Msgs";
	private String dumpTopic00Msgs = "dumpTopic00Msgs";
	private String dumpTopic11Msgs = "dumpTopic11Msgs";
	private String topic4DurableSubscriber = "topic4DurableSubscriber";
	private String durableSubscriberName = "durableSubscriberName";
	private String connFactName = "ConnectionFactory";
	
	private ConnectionFactory connFact;
	private JmsDumpDestination destDumper;
	private JmsDurableSubscriberManipulator durableSubsManipulator;
	private String clientId = "gursev";
	
	//The "session" and "connection" are used to send messages to the test queues and topics.
	// They are never passed on to the JmsDumpDestination object
	private Session session;
	private Connection connection;
	
	
	private InitialContext getInitialContext() throws JmsDiggerException
	{
		JmsInitialContextFactory contextFactory = new JmsInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://192.168.127.130:61616");
		contextFactory.addConnectionFactory(connFactName);
		contextFactory.addQueue(dumpQueue11Msgs, "jms."+dumpQueue11Msgs);
		contextFactory.addQueue(dumpQueue00Msgs, "jms."+dumpQueue00Msgs);
		contextFactory.addTopic(dumpTopic00Msgs, "jms."+dumpTopic00Msgs);
		contextFactory.addTopic(dumpTopic11Msgs, "jms."+dumpTopic11Msgs);
		contextFactory.addTopic(topic4DurableSubscriber, "jms."+topic4DurableSubscriber);
		InitialContext ctxx = contextFactory.getInitialContext();
		return ctxx;
	}
	
	
	@Before
	public void setUp() throws Exception {
		ctx = getInitialContext();
		connFact = (ConnectionFactory) ctx.lookup(connFactName);
		connection = (Connection) connFact.createConnection();
	}

	@After
	public void tearDown() throws Exception {
		if(destDumper != null) 
			destDumper.close();
	}
	
//	@Test 
//	public void testDumpQueue00MsgsForNoMessage() throws JMSException, IOException, Exception {
//		String qName = dumpQueue00Msgs;
//		destDumper = new JmsDumpDestination(ctx, qName, connFactName);
//		destDumper.setMsgCountToDump(0);
//		destDumper.init();	
//		try {
//		destDumper.dump();
//		} catch(Exception ex) {
//			//gobble it up
//		}
//		destDumper.close();
//		assertEquals(0, destDumper.getMsgsWritten());
//	}
	
		
	private void sendMessages(Session session, MessageProducer mp) throws NamingException, JMSException {
		byte[] b = {0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47};
		Message msg = session.createMessage();
		mp.send(msg);
		msg = session.createMessage();
		mp.send(msg);
		
		TextMessage tmsg = (TextMessage) session.createTextMessage();
		tmsg.setText("text message by gursev");
		tmsg.setStringProperty(ScheduledMessage.AMQ_SCHEDULED_CRON, "* * * * *");
		String s = new IdGenerator().generateId();
		tmsg.setStringProperty(ScheduledMessage.AMQ_SCHEDULED_ID, s);
		System.out.println(s);
		mp.send(tmsg);
		tmsg = (TextMessage) session.createTextMessage();
		tmsg.setText("text message by gursev");
		mp.send(tmsg);
		
		ObjectMessage omsg = (ObjectMessage) session.createObjectMessage(true);
		omsg.setObject(new RandomPerson());
		mp.send(omsg);
		
		
		MapMessage mmsg = (MapMessage) session.createMapMessage();
		mmsg.setBoolean("boolean", false);

		
		mmsg.setByte("byte", (byte) 0x44);
		mmsg.setChar("char", 'c');
		mmsg.setFloat("float", (float) 4.444);
		mmsg.setInt("int", 33);
		mmsg.setBytes("bytes[]", b);
		mp.send(mmsg);
		mmsg = (MapMessage) session.createMapMessage();
		mmsg.setBoolean("boolean", false);
		mmsg.setByte("byte", (byte) 0x44);
		mmsg.setChar("char", 'c');
		mmsg.setFloat("float", (float) 4.444);
		mmsg.setInt("int", 33);
		mmsg.setBytes("bytes[]", b);
		mp.send(mmsg);
		
		
		BytesMessage bmsg = (BytesMessage) session.createBytesMessage();
		bmsg.writeBytes(b);
		mp.send(bmsg);

		bmsg = (BytesMessage) session.createBytesMessage();
		bmsg.writeBytes(b);
		mp.send(bmsg);
		
		StreamMessage smsg = (StreamMessage) session.createStreamMessage();
		smsg.writeBoolean(true);
		smsg.writeByte((byte) 0x44);
		smsg.writeChar('c');
		smsg.writeFloat((float) 4.444);
		smsg.writeInt(3333);
		smsg.writeBytes(b);		
		mp.send(smsg);
		
		smsg = (StreamMessage) session.createStreamMessage();
		smsg.writeBoolean(true);
		smsg.writeByte((byte) 0x44);
		smsg.writeChar('c');
		smsg.writeFloat((float) 4.444);
		smsg.writeInt(3333);
		smsg.writeBytes(b);		
		mp.send(smsg);
	}
	
	
	@Test 
	public void testDumpTopic00MsgsForNoMessage() throws JMSException, IOException, Exception {
		String tName = dumpTopic00Msgs;
		destDumper = new JmsDumpDestination(ctx, tName, connFactName);
		destDumper.setTargetDirectory("/Users/Consultant/jmsdigger");
		destDumper.init();
		
		destDumper.dump();

		//Sleep to ensure that every message is read
		Thread.sleep(5000);
		destDumper.close();
		assertEquals(0, destDumper.getMsgsWritten());
	}
	
	
	@Test
	public void testDumpTopic11MsgsFor11Messages() throws JMSException, IOException, Exception {
		Topic topic = (Topic) ctx.lookup(dumpTopic11Msgs);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer mp = session.createProducer(topic);
		
		destDumper = new JmsDumpDestination(ctx, dumpTopic11Msgs, connFactName);
		destDumper.setTargetDirectory("/Users/Consultant/jmsdigger");

		destDumper.init();
		
		//Initiating dump before sending messages. This ensures that onMessage has started listening before messages are sent
		destDumper.dump();
		
		//Sending messages after initiating the dump process so messages are read
		sendMessages(session, mp);
		
		//Sleep to ensure that every message is read
		Thread.sleep(10000);
		destDumper.close();
		assertEquals(11, destDumper.getMsgsWritten());
	}
	
	
	@Test
	public void testDumpQueue11MsgsFor11Messages() throws JMSException, IOException, Exception {
		Queue queue = (Queue) ctx.lookup(dumpQueue11Msgs);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer mp = session.createProducer(queue);
		sendMessages(session, mp);
		
		destDumper = new JmsDumpDestination(ctx, dumpQueue11Msgs, connFactName);
		destDumper.setTargetDirectory("/Users/Consultant/jmsdigger");
		destDumper.setMsgCountToDump(11);
	
		destDumper.init();
		destDumper.dump();
		destDumper.close();
		assertEquals(11, destDumper.getMsgsWritten());
	}
	
	/**
	 * This test works with two classes
	 * 1. Durable subscriber manipulation class
	 * 2. Dump destination.
	 * @throws NamingException 
	 * @throws JMSException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 */
	@Test
	public void testDurableSubscriberDump() throws NamingException, JMSException, InterruptedException, IOException, IllegalAccessException {
		Topic topic = (Topic) ctx.lookup(topic4DurableSubscriber);
		
		durableSubsManipulator = new JmsDurableSubscriberManipulator(ctx, topic4DurableSubscriber, connFactName);
		durableSubsManipulator.init(clientId);
		durableSubsManipulator.createDurableSubscriber(durableSubscriberName);
		durableSubsManipulator.close(); // Important to close as a only one connection can use a single client ID. 
		
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer mp = session.createProducer(topic);
		mp.setTimeToLive(0);
		sendMessages(session, mp);
		
		//Closing to ensure that dump method connectivity with same ID does not error out.
		session.close();
		connection.close();
		
		destDumper = new JmsDumpDestination(ctx, topic4DurableSubscriber, connFactName);
		destDumper.setTargetDirectory("/Users/Consultant/jmsdigger");
		destDumper.setClientId(clientId);
		destDumper.setDurableSubscriberName(durableSubscriberName);
		destDumper.init();
		destDumper.dump();

		Thread.sleep(10000);
		destDumper.close();
		assertEquals(11, destDumper.getMsgsWritten());
		
	}
	
	@Test
	public void zSendMessagesToDurableSubscriber() throws NamingException, JMSException {
		Topic topic = (Topic) ctx.lookup(topic4DurableSubscriber);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer mp = session.createProducer(topic);
		for(int i = 0; i < 10; i ++) {
		//mp.setTimeToLive(0);
			sendMessages(session, mp);
		}
		session.close();
		connection.close();
		destDumper = null;

	}
	
	@Test 
	public void zSendMessagesToTopic11Msgs() throws NamingException, JMSException {
		Topic topic = (Topic) ctx.lookup(dumpTopic11Msgs);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer mp = session.createProducer(topic);
		sendMessages(session, mp);
		destDumper = null;
	}
	
	@Test
	public void zSendMessagesToQueue() throws NamingException, JMSException {
		Queue queue = (Queue) ctx.lookup(dumpQueue11Msgs);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer mp = session.createProducer(queue);
		sendMessages(session, mp);
		destDumper = null;
	}

	
}
