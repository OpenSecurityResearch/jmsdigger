package com.mcafee;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import org.apache.commons.codec.binary.Hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Objects of this class write Message and its data to flat files. It determines
 * the type of message and then writes data as per different headers. Types of
 * messages supported are:<br/> 
 * 1. Message <br/> 
 * 2. TextMessage <br/>  
 * 3. ObjectMessage <br/> 
 * 4. BytesMessage<br/> 
 * 5. Stream Message <br/> 
 * 6. MapMessage<br/> 
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 * 
 */

public class JmsWriter {

	private static final Logger LOG = LoggerFactory.getLogger(JmsWriter.class);
	
	private static String fileSeparator;
	private String targetDir;
	private boolean targetDirExists;
	private static String msgSeparator; // used as a separator between messages
										// being dumped

	private static String longLineOfChars = "======================================";

	private String metaPath;
	private PrintWriter metadataFileHandle;	

	private String objMsgFilePath;
	private ObjectOutputStream objMsgFileHandle;
	private FileOutputStream bytesMsgFileHandleBin;
	
	private String byteMsgFilePath;
	private String byteMsgFilePathBin;
	private PrintWriter bytesMsgFileHandle;
	private String bytesFileHeader = "BytesMessage as Java Arrays\n" + longLineOfChars + "\n";
	
	private String textMsgFilePath;
	private PrintWriter textMsgFileHandle;
	private String textFileHeader = "TextMessages \n" + longLineOfChars + "\n";
	
	private String streamMsgFilePath;
	private PrintWriter streamMsgFileHandle;
	private String streamFileHeader = "StreamMessage as Java Arrays\n" + longLineOfChars + "\n";
	
	private String mapMsgFilePath;
	private PrintWriter mapMsgFileHandle;
	private String mapFileHeader = "MapMessage as Java Arrays\n" + longLineOfChars + "\n";


	private int textMsgsWritten = 0;
	private int bytesMsgsWritten = 0;
	private int objMsgsWritten = 0;
	private int streamMsgsWritten = 0;
	private int mapMsgsWritten = 0;
	private int msgsWritten = 0;

	private String identifier;

	static {
		msgSeparator = "~-~-~-~-~-~-~-~-~-~" + (new Date()).getTime() + "\n";
	}

	public JmsWriter(String targetDir, String identifier) throws JmsDiggerException {
		this.targetDir = targetDir;
		targetDirExists = false;
		fileSeparator = System.getProperty("file.separator");
		try {
			(new File(targetDir)).mkdirs();
		} catch (Exception ex) {
			LOG.error("Target directory could not be created", ex);
			throw JmsHelper.buildJmsDiggerException("Target directory could not be created");
		}
		
		this.identifier = identifier;
	}
	
	public void init() throws FileNotFoundException, IOException {
		LOG.debug("Entering init");
		initFileHandles();
		LOG.debug("Leaving init");
	}

	private void initFileHandles() throws FileNotFoundException, IOException {
		// time in seconds since epoch
		LOG.debug("Entering initFileHandles");
		long dt = ((new Date()).getTime());
		//System.out.println(dt);
		metaPath = targetDir + fileSeparator + identifier + "-MetaData-" + dt + ".txt";
		objMsgFilePath = targetDir + fileSeparator + identifier + "-ObjectMessage-" + dt + ".obj";
		byteMsgFilePath = targetDir + fileSeparator + identifier + "-BytesMessage-" + dt + ".txt";
		byteMsgFilePathBin = targetDir + fileSeparator + identifier	+ "-BytesMessage-" + dt + ".bin";
		textMsgFilePath = targetDir + fileSeparator + identifier + "-TextMessage-" + dt + ".txt";
		streamMsgFilePath = targetDir + fileSeparator + identifier + "-StreamMessage-" + dt + ".txt";
		mapMsgFilePath = targetDir + fileSeparator + identifier + "-MapMessage-" + dt + ".txt";

		try {
			metadataFileHandle = new PrintWriter(new FileOutputStream(metaPath), true);
			objMsgFileHandle = new ObjectOutputStream(new FileOutputStream(objMsgFilePath));
			bytesMsgFileHandle = new PrintWriter(byteMsgFilePath);
			bytesMsgFileHandleBin = new FileOutputStream(byteMsgFilePathBin);
			textMsgFileHandle = new PrintWriter(textMsgFilePath);
			streamMsgFileHandle = new PrintWriter(streamMsgFilePath);
			mapMsgFileHandle = new PrintWriter(mapMsgFilePath);
		} catch (FileNotFoundException ex) {
			LOG.error("Output file handle could not be creatd", ex);
			throw ex;
		} catch (IOException ex) {
			LOG.error("An IO Error occured while creting ObjectOutputStream", ex);
			throw ex;
		}
		
		LOG.debug("Leaving initFileHandles");
	}

	private String byteArrayToHexString(byte[] b) {
		LOG.debug("Entering byteArrayToHexString");
		// Converts a byte array to string representation of hex digits
		// { 0x12, 0x23, 0x32, 0xA5 }
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
		LOG.debug("Leaving byteArrayToHexString");
		return sb.toString();
	}

	private void writeBytesMessage(BytesMessage msg) throws JmsDiggerException {
		LOG.debug("Entering writeBytesMessage");
		int len = -1;
		byte[] binSeparator = { 0x0A, 0x0A, 0x0D, 0x0D, 0x0D, 0x0D, 0x0A};

		try {
			len = (int) msg.getBodyLength();
			if (len == 0) {
				bytesMsgFileHandle.println("{ }");
				return;
			}
	
			byte[] body = new byte[len];
			msg.readBytes(body);
	
			bytesMsgFileHandleBin.write(body);
			bytesMsgFileHandleBin.write(binSeparator);
			String bodyAsString = byteArrayToHexString(body);
			bytesMsgFileHandle.print(bytesFileHeader);
			bytesMsgFileHandle.println(bodyAsString);
			bytesMsgFileHandle.println(msgSeparator);
		} catch (JMSException ex) {
			LOG.info("An error occured while retreving content from ByteMessage", ex);
			throw JmsHelper.buildJmsDiggerException("An error occured while retreving content from ByteMessage", ex);
		} catch (IOException ex) {
			LOG.info("An error occured while writing ByteMessage to file", ex);
			throw JmsHelper.buildJmsDiggerException("An error occured while writing ByteMessage to file", ex);
		}
		LOG.debug("Leaving writeBytesMessage");

	}

	private void writeStreamMessage(StreamMessage msg) throws JmsDiggerException {
		LOG.debug("Entering writeStreamMessage");
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		Object o;
		
		try {
			while (true) {
				o = msg.readObject();
				if (o instanceof byte[])
					sb.append(byteArrayToHexString((byte[]) o));
				else
					sb.append(o);
				sb.append(", ");
			}
		} catch (MessageEOFException ex) {
			sb.deleteCharAt(sb.length() - 2);
			// Do nothing. End of StreamMessage content has reached.
		} catch (JMSException ex) {
			LOG.info("An error occured while reading Object from StreamMessage", ex);
			throw JmsHelper.buildJmsDiggerException("An error occured while reading Object from StreamMessage", ex);
		}
		
		sb.append("}");
		
		streamMsgFileHandle.println(streamFileHeader);
		streamMsgFileHandle.print(bytesFileHeader);
		streamMsgFileHandle.println(sb.toString());
		streamMsgFileHandle.println(msgSeparator);
		
		LOG.debug("Leaving writeStreamMessage");
	}

	private void writeMapMessage(MapMessage msg) throws JmsDiggerException {
		LOG.debug("Entering writeMapMessage");
		StringBuilder sb = new StringBuilder();
		Enumeration e = null;
		String name;
		Object value;
		
		try {			
			sb.append(JmsHelper.mapMessageToString(msg, "Message name value pairs : "));
			sb.append(msgSeparator + "\n");	
		} catch (JMSException ex) {
			LOG.info("An error occured while reading MapMessage", ex);
			throw JmsHelper.buildJmsDiggerException("An error occured while reading MapMessage", ex);
		}
		mapMsgFileHandle.print(mapFileHeader);
		mapMsgFileHandle.println(sb.toString());
		
		LOG.debug("Leaving writeMapMessage");

	}

	private void writeObjectMessage(ObjectMessage msg) throws JmsDiggerException {
		LOG.debug("Entering writeObjectMessage");
		try {
			objMsgFileHandle.writeObject(msg.getObject());
		} catch (JMSException ex) {
			LOG.info("An error occured while retreving content from ObjectMessage", ex);
			throw JmsHelper.buildJmsDiggerException("An error occured while retreving content from ObjectMessage", ex);
		} catch (IOException ex) {
			LOG.info("An error occured while writing ObjectMessage to file", ex);
			throw JmsHelper.buildJmsDiggerException("An error occured while writing ObjectMessage to file", ex);
		}
		LOG.debug("Leaving writeObjectMessage");
	}

	private void writeTextMessage(TextMessage msg) throws JmsDiggerException {
		LOG.debug("Entering writeTextMessage");
		try {
			textMsgFileHandle.print(textFileHeader);
			textMsgFileHandle.println(msg.getText());
			textMsgFileHandle.println(msgSeparator);
		} catch (JMSException ex) {
			LOG.info("An error occured while processing TextMessage", ex);
			throw JmsHelper.buildJmsDiggerException("An error occured while processing TextMessage", ex);
		}
		LOG.debug("Leaving writeTextMessage");
	}

	/**
	 * Analyze the message type against all the JMS message types supported and
	 * write metadata as per the message type
	 * 
	 * @param msg
	 * @param result
	 * @throws JmsDiggerException 
	 * @throws IOException
	 */
	public void writeMsg(Message msg) throws JmsDiggerException {
		LOG.debug("Entering writeMsg");
		if (msg == null)
			throw new IllegalArgumentException("Message parameter is null");

		JmsMsgType type = null;
		StringBuilder metaInfo = new StringBuilder();

		try {
			if (msg instanceof BytesMessage) {
				LOG.debug("Message Type : BytesMessage");
				type = JmsMsgType.BYTES;
				metaInfo.append("Message Type : BytesMessage");
				metaInfo.append("\n");
				metaInfo.append("Message length in Bytes : " + ((BytesMessage) msg).getBodyLength());
				metaInfo.append("\n");
				writeBytesMessage((BytesMessage) msg);
				bytesFileHeader = "";
				bytesMsgsWritten++;
			}

			if (type == null && msg instanceof TextMessage) {
				LOG.debug("Message Type : TextMessage");
				type = JmsMsgType.TEXT;
				metaInfo.append("Message Type : TextMessage");
				metaInfo.append("\n");
				writeTextMessage((TextMessage) msg);
				textFileHeader = "";
				textMsgsWritten++;
			}

			if (type == null && msg instanceof StreamMessage) {
				LOG.debug("Message Type : StreamMessage");
				type = JmsMsgType.STREAM;
				metaInfo.append("Message Type : StreamMessage");
				metaInfo.append("\n");
				writeStreamMessage((StreamMessage) msg);
				streamFileHeader = "";
				streamMsgsWritten++;
			}

			if (type == null && msg instanceof MapMessage) {
				LOG.debug("Message Type : MapMessage");
				type = JmsMsgType.MAP;
				metaInfo.append("Message Type : MapMessage");
				metaInfo.append("\n");
				writeMapMessage((MapMessage) msg);
				mapFileHeader = "";
				mapMsgsWritten++;
			}

			if (type == null && msg instanceof ObjectMessage) {
				LOG.debug("Message Type : ObjectMessage");
				type = JmsMsgType.OBJECT;
				metaInfo.append("Message Type : ObjectMessage");
				metaInfo.append("\n");
				writeObjectMessage((ObjectMessage) msg);
				objMsgsWritten++;
			}

			if (type == null && msg instanceof Message) {
				LOG.debug("Message Type : Message");
				type = JmsMsgType.MESSAGE;
				metaInfo.append("Message Type : Message");
				metaInfo.append("\n");
			}

			// All the generic stuff comes here
			metaInfo.append("JMSCorrelationID : " + msg.getJMSCorrelationID() + "\n");
			metaInfo.append("JMSDeliveryMode : " + msg.getJMSDeliveryMode() + "\n");
			metaInfo.append("JMSExpiration : " + msg.getJMSExpiration() + "\n");
			metaInfo.append("JMSPriority : " + msg.getJMSPriority() + "\n");
			metaInfo.append("JMSMessageID : " + msg.getJMSMessageID() + "\n");
			metaInfo.append("JMSTimeStamp : " + msg.getJMSTimestamp() + "\n");
			metaInfo.append("JMSType : " + msg.getJMSType() + "\n");
			metaInfo.append("JMSDestination : " + msg.getJMSDestination() + "\n");
			metaInfo.append("JMSReplyTo : " + msg.getJMSReplyTo() + "\n");
			metaInfo.append(msgSeparator);
			metadataFileHandle.println(metaInfo.toString());
			msgsWritten++;

		} catch (JMSException ex) {
			LOG.info("A JMS operation failed", ex);
			throw JmsHelper.buildJmsDiggerException("A JMS operation failed", ex);
		}
		
		LOG.debug("Leaving writeMsg");

	}

	private void deleteFile(String fname) {
		LOG.debug("Entering deleteFile");
		LOG.debug("Deleting file " + fname);
		File f = new File(fname);
		f.delete();
		LOG.debug("Leaving deleteFile");
	}
	
	private String getWriteCountBreakup() {
		StringBuilder sb = new StringBuilder();
		sb.append("ObjectMessages written: " + objMsgsWritten + ", ");
		sb.append("BytesMessages written: " + bytesMsgsWritten + ", ");
		sb.append("StreamMessages written: " + streamMsgsWritten + ", ");
		sb.append("MapMessages written: " + mapMsgsWritten + ", ");
		sb.append("TextMessages written: " + textMsgsWritten);		
		return sb.toString();
	}

	public void close() throws IOException {
		LOG.debug("Entering close");
		if (msgsWritten == 0)
			metadataFileHandle.println("No messages were written");

		objMsgFileHandle.flush();
		bytesMsgFileHandle.flush();
		textMsgFileHandle.flush();
		bytesMsgFileHandleBin.flush();
		streamMsgFileHandle.flush();
		mapMsgFileHandle.flush();
		metadataFileHandle.flush();
		
		objMsgFileHandle.close();
		bytesMsgFileHandle.close();
		textMsgFileHandle.close();
		bytesMsgFileHandleBin.close();
		streamMsgFileHandle.close();
		mapMsgFileHandle.close();
		metadataFileHandle.close();

		try {
			if (objMsgsWritten == 0)
				deleteFile(objMsgFilePath);

			if (bytesMsgsWritten == 0) {
				deleteFile(byteMsgFilePath);
				deleteFile(byteMsgFilePathBin);
			}

			if (streamMsgsWritten == 0) {
				deleteFile(streamMsgFilePath);
			}

			if (mapMsgsWritten == 0) {
				deleteFile(mapMsgFilePath);
			}

			if (textMsgsWritten == 0) {
				deleteFile(textMsgFilePath);
			}
		} catch (Exception ex) {
			LOG.debug("File delete operation failed", ex);
			// Ignoring a file delete exceptions
		}
		
		LOG.info(getWriteCountBreakup());
		LOG.debug("Leaving close");
	}

}
