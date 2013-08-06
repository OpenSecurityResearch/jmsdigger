package com.mcafee.gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcafee.JmsDestination;
import com.mcafee.JmsDiggerException;
import com.mcafee.JmsDumpDestination;
import com.mcafee.JmsHelper;
import com.mcafee.JmsLoginInfo;
import com.mcafee.MessageCountUpdater;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsDumpDestinationTab extends JPanel {
	

	/**
	 * 
	 */
	private static final Logger LOG = LoggerFactory.getLogger(JmsDumpDestinationTab.class);
	private static final long serialVersionUID = 1L;
	private String title;
	private JmsConfigTab jmsConfigTab;
	private static int space = 5;
	private JLabel writtenMsgCountLabel;
	private JTextField durableSubsNameInput;
	private JTextField clientIdInput;
	private JTextField messageSelectorInput;
	private JTextField destNameInput;
	private JButton startButton;
	private JButton stopButton;
	private SpinnerNumberModel spinnerModel;
	private JSpinner msgCountSpinner;
	private JTextField outputDirPath;
	private JButton outputDirSelectorButton;
	private JRadioButton topicRadio;
	private JRadioButton queueRadio;
	private JRadioButton durableRadio;
	private ButtonGroup bg;
	private DestinationDumpWorker destinationDumpWorker;
	
	public String getTitle() {
		return title;
	}
	
	public void enableDisableInputs(boolean status) {
		clientIdInput.setEditable(status);
		durableSubsNameInput.setEditable(status);		
	}
	
	public void enableInputs() {
		enableDisableInputs(true);
	}
	
	public void disableInputs() {
		enableDisableInputs(false);
		clientIdInput.setText("");
		durableSubsNameInput.setText("");
	}
	
	public JmsDumpDestinationTab(JmsConfigTab jmsConfigTab) {
		this.jmsConfigTab = jmsConfigTab;
		GridBagConstraints masterGbc = new GridBagConstraints();
		JPanel dumpDestinationPanel = new JPanel(new GridBagLayout());
		
		Insets inset = new Insets(space, space, space, space);
		masterGbc.insets = inset;
		GridBagConstraints gbc = new GridBagConstraints();
		
		JLabel dstNameLabel = new JLabel("Destination Name");
		destNameInput = new JTextField();
		dstNameLabel.setHorizontalAlignment(JLabel.RIGHT);
		
		JLabel dstTypeLabel = new JLabel("Destination Type");
		dstTypeLabel.setHorizontalAlignment(JLabel.RIGHT);		
		topicRadio = new JRadioButton("Topic");
		topicRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disableInputs();
			}
		});
		
		
		topicRadio.setSelected(true);
		queueRadio = new JRadioButton("Queue");
		queueRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disableInputs();
			}
		});

		
		durableRadio = new JRadioButton("Durable Subscriber");
		durableRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableInputs();
			}
		});

		bg = new ButtonGroup();
		bg.add(topicRadio);
		bg.add(queueRadio);
		bg.add(durableRadio);
		
		JPanel dumpRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		gbc.gridx = 0;
		gbc.gridy = 0;
		dumpRadioPanel.add(topicRadio, gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		dumpRadioPanel.add(queueRadio, gbc);
		gbc.gridx = 2;
		gbc.gridy = 0;
		dumpRadioPanel.add(durableRadio, gbc);
		

		JLabel messageSelectorLabel = new JLabel("Message Selector");
		messageSelectorInput = new JTextField();
		messageSelectorLabel.setHorizontalAlignment(JLabel.RIGHT);

		JLabel clientIdLabel = new JLabel("Client ID (for durable Subscribers)");
		clientIdInput = new JTextField();
		clientIdLabel.setHorizontalAlignment(JLabel.RIGHT);
		//JPasswordField confirmPasswordInput = new JPasswordField();
		
		JLabel durableSubsNameLabel = new JLabel("Durable Subscriber Name");
		durableSubsNameInput = new JTextField();
		durableSubsNameLabel.setHorizontalAlignment(JLabel.RIGHT);
		
		durableSubsNameLabel.setHorizontalAlignment(JLabel.RIGHT);
		
		JLabel outputDirLabel = new JLabel("Output Directory");
		outputDirLabel.setHorizontalAlignment(JLabel.RIGHT);
		outputDirSelectorButton = new JButton("Select");
		outputDirSelectorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setCurrentDirectory(new File("/"));
				boolean validDirectory = false;
				//Scanner fileScanner;

				while(validDirectory != true) {
					int result = fileChooser.showOpenDialog(null);
					if(result == JFileChooser.CANCEL_OPTION)
						return;
					if(result == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						String filePath = file.getAbsolutePath();
						File check = new File(filePath);
						if(!check.exists()) {
							JOptionPane.showMessageDialog(null, "Invalid directory path", "Failure", JOptionPane.ERROR_MESSAGE);
							
						} else {
							outputDirPath.setText(file.getAbsolutePath());
							validDirectory = true;
						}
					}
				}

			}
		});
		
		outputDirPath = new JTextField();
		String separator = System.getProperty("file.separator");
		outputDirPath.setText(System.getProperty("user.home")+separator+"jmsdigger"); 
		
		outputDirPath.setEditable(false);
		//outputDirPath.setEditable(false);
		JPanel outputDirSelectorPanel = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		outputDirSelectorPanel.add(outputDirPath, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		outputDirSelectorPanel.add(outputDirSelectorButton, gbc);
		
		JLabel msgCountLabel = new JLabel("Messages to Dump (Count)");
		msgCountLabel.setHorizontalAlignment(JLabel.RIGHT);
		spinnerModel = new SpinnerNumberModel(10, 0, 9999, 1);
		JPanel countStartStopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		msgCountSpinner = new JSpinner(spinnerModel);	
		startButton = new JButton("Start Dump");
		startButton.addActionListener(new TriggerDestinationDump());
		stopButton = new JButton("Stop Dump");

		stopButton.setEnabled(false);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;

		countStartStopPanel.add(msgCountSpinner, gbc);
		
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		countStartStopPanel.add(startButton, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		countStartStopPanel.add(stopButton, gbc);
		durableSubsNameLabel.setHorizontalAlignment(JLabel.RIGHT);

		JLabel writtenMsgLabel = new JLabel("Messages Written");
		writtenMsgLabel.setHorizontalAlignment(JLabel.RIGHT);
		writtenMsgCountLabel = new JLabel("--");
		writtenMsgCountLabel.setHorizontalAlignment(JLabel.LEFT);
	
	
		masterGbc.gridx = 0;
		masterGbc.gridy = 0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		masterGbc.weightx = 0.0;
		dumpDestinationPanel.add(dstNameLabel, masterGbc);
		masterGbc.gridx = 1;
		masterGbc.gridy = 0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		masterGbc.weightx = 1.0;
		dumpDestinationPanel.add(destNameInput, masterGbc);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 1;
		masterGbc.weightx = 0.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(dstTypeLabel, masterGbc);
		masterGbc.gridx = 1;
		masterGbc.gridy = 1;
		masterGbc.weightx = 1.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(dumpRadioPanel, masterGbc);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 2;
		masterGbc.weightx = 0.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(messageSelectorLabel, masterGbc);
		masterGbc.gridx = 1;
		masterGbc.gridy = 2;
		masterGbc.weightx = 1.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(messageSelectorInput, masterGbc);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 3;
		masterGbc.weightx = 0.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(clientIdLabel, masterGbc);
		masterGbc.gridx = 1;
		masterGbc.gridy = 3;
		masterGbc.weightx = 1.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(clientIdInput, masterGbc);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 4;
		masterGbc.weightx = 0.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(durableSubsNameLabel, masterGbc);
		masterGbc.gridx = 1;
		masterGbc.gridy = 4;
		masterGbc.weightx = 0.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(durableSubsNameInput, masterGbc);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 5;
		masterGbc.weightx = 0.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(outputDirLabel, masterGbc);
		masterGbc.gridx = 1;
		masterGbc.gridy = 5;
		masterGbc.weightx = 1.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(outputDirSelectorPanel, masterGbc);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 6;
		masterGbc.weightx = 0.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(msgCountLabel, masterGbc);
		masterGbc.gridx = 1;
		masterGbc.gridy = 6;
		masterGbc.weightx = 1.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(countStartStopPanel, masterGbc);

		masterGbc.gridx = 0;
		masterGbc.gridy = 7;
		masterGbc.weightx = 0.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(writtenMsgLabel, masterGbc);
		masterGbc.gridx = 1;
		masterGbc.gridy = 7;
		masterGbc.weightx = 1.0;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		dumpDestinationPanel.add(writtenMsgCountLabel, masterGbc);
		
		masterGbc = new GridBagConstraints();
		masterGbc.gridx = 0;
		masterGbc.gridy = 0;
		masterGbc.anchor = GridBagConstraints.NORTHWEST;
		this.add(dumpDestinationPanel, masterGbc);
		
		disableInputs();
		this.title = "Dump Destinations";
		
	}
	
	class TriggerDestinationDump implements ActionListener {
		
		public void actionPerformed(ActionEvent arg0) {
			JmsDestination destinationType = JmsDestination.TOPIC;
			if(JmsHelper.isStringNullOrEmpty(destNameInput.getText())) {
				JOptionPane.showMessageDialog(null, "Destination name is empty", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if(durableRadio.isSelected()) {
				if(JmsHelper.isStringNullOrEmpty(clientIdInput.getText()) || JmsHelper.isStringNullOrEmpty(durableSubsNameInput.getText())) {
					JOptionPane.showMessageDialog(null, "Client ID or Durable subscriber name not present", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				destinationType = JmsDestination.DURABLESUBSCRIBER;
			} else {
				if(topicRadio.isSelected()) {
					destinationType = JmsDestination.TOPIC;
				} else {
					destinationType = JmsDestination.QUEUE;
				}
			}

			try {
					//System.out.println("here");
					destinationDumpWorker = new DestinationDumpWorker(destinationType);
					for(ActionListener a : stopButton.getActionListeners()) {
						stopButton.removeActionListener(a);
					}
					stopButton.addActionListener(new ActionListener() {	
						public void actionPerformed(ActionEvent arg0) {
							destinationDumpWorker.cancel(true);
						}
					});
					destinationDumpWorker.execute();
					writtenMsgCountLabel.setText("0");
					disableButtons();
				} catch (JmsDiggerException ex) {
					String errStr = "An execption occured while triggering destination dump";
					LOG.info(errStr, ex);
					JmsGuiCommon.showErrorAndLogMessage(ex.getMessage());
				} catch (NamingException ex) {
					String errStr = "An NamingException occured while triggering a destination dump";
					LOG.info(errStr, ex);
					JmsGuiCommon.showErrorAndLogMessage(ex.getMessage());
				} catch (JMSException ex) {
					LOG.info("", ex);
					JmsGuiCommon.showErrorAndLogMessage(ex.getMessage());
				}

		}
		
		private void disableButtons() {
			startButton.setEnabled(false);
			outputDirSelectorButton.setEnabled(false);
			stopButton.setEnabled(true);
		}
	}
	
	class DestinationDumpWorker extends SwingWorker<String, String> {
		private String destinationName; 
		private JmsDestination destinationType; 
		private String msgSelector; 
		private String clientId; 
		private String durableSubscriberName; 
		private String outputDirectory; 
		private int count;
		private JmsDumpDestination destinationDumper;
		private JmsConfig jmsConfig;
		private InitialContext initialContext;
		//private Connection connection;
		//private ConnectionFactory connFact;
		private MessageCountUpdater messageCountUpdateObject;
		private JmsLoginInfo loginInfo;

		
		public DestinationDumpWorker(JmsDestination destinationType) throws NamingException, JMSException {
			jmsConfig = jmsConfigTab.getJmsConfig();
			String connectionFactoryName = jmsConfig.getConnFactName();
			this.destinationName = destNameInput.getText(); 
			this.destinationType = destinationType; 
			this.msgSelector = messageSelectorInput.getText(); 
			this.clientId = clientIdInput.getText(); 
			this.durableSubscriberName = durableSubsNameInput.getText();
			this.outputDirectory = outputDirPath.getText(); 
			this.count = (Integer) msgCountSpinner.getValue();
			
			if(this.destinationType == JmsDestination.QUEUE)
				jmsConfig.addQueue(this.destinationName);
			else
				jmsConfig.addTopic(this.destinationName);
			this.initialContext = jmsConfig.buildInitialContext();
			this.loginInfo = jmsConfig.getLoginInfo();
			//this.connFact = (ConnectionFactory) initialContext.lookup(connectionFactoryName);
			//connection = (Connection) connFact.createConnection();
			messageCountUpdateObject = new MessageCountUpdater(writtenMsgCountLabel);
			destinationDumper = new JmsDumpDestination(this.initialContext, this.destinationName, connectionFactoryName, this.msgSelector, this.loginInfo);
			destinationDumper.setTargetDirectory(this.outputDirectory);
			destinationDumper.setClientId(this.clientId);
			destinationDumper.setDurableSubscriberName(this.durableSubscriberName);
			destinationDumper.setMsgCountToDump(count);
			destinationDumper.setMessageCountUpdater(messageCountUpdateObject);
		}
		
		private void enableButtons() {
			startButton.setEnabled(true);
			outputDirSelectorButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
		
		@Override
		protected String doInBackground() throws Exception {
			// System.out.println("Doing in background");
			//Thread.sleep(5*1000);
			destinationDumper.init();
			destinationDumper.dump();
			return null;
		}
		
		@Override 
		public void done() {
			Thread t = new Thread(
					new Runnable() { 
						public void run() {				
							while(true) {
								if(!isCancelled()) {
									if(!destinationDumper.isDumpComplete()) {
										try {
											//System.out.println(destinationDumper.isDumpComplete() + "\tSleeping now ======================\t" + isCancelled());
											Thread.sleep(1 * 1000);										
										} catch (InterruptedException e) {
											LOG.info("The thread was interrupted", e);
											JmsGuiCommon.showErrorAndLogMessage(e.getMessage());
										}
										continue;
									}
								}
								try {
									enableButtons();
									destinationDumper.close();
									break;
								} catch (JMSException ex) {
									LOG.info("Destination Dumper could not be closed", ex);
									JmsGuiCommon.showErrorAndLogMessage(ex.getMessage());
								} catch (IOException ex) {
									LOG.info("An IOException has occured", ex);
									JmsGuiCommon.showErrorAndLogMessage(ex.getMessage());
								}
							}
						}
					});
			t.start();
		}
	}
}
