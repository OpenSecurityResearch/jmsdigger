package com.mcafee.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import javax.naming.InitialContext;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import com.mcafee.AMQOps;
import com.mcafee.JmsAuthentication;
import com.mcafee.JmsDestination;
import com.mcafee.JmsDiggerException;
import com.mcafee.JmsHelper;
import com.mcafee.JmsLoginInfo;
import com.mcafee.JmsPasswordOps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class AmqOpsTab extends JPanel {
	private static final long serialVersionUID = 1L;
	private String title;
	private static int space = 5;
	private JmsConfigTab jmsConfigTab;
	private JmsConfig jmsConfig;
	private InitialContext initialContext;
	private JmsLoginInfo loginInfo;
	private JmsAuthentication jmsAuthn;
	private boolean boolResult;
	
	private JTextField destNameInput;
	private JButton createDestButton;
	private JTextArea outputArea;
	private JRadioButton queueRadio;
	private JRadioButton topicRadio;
	private ButtonGroup radioGroup;
	private JTextField obtainStatsInput;
	private JRadioButton queueStats;
	private JRadioButton topicStats;
	private JRadioButton brokerStats;
	private JRadioButton subscriptionStats;
	private JButton statsButton;
	private JList<String> decKeysList;
	private JList<String> encPasswordList;
	private JButton loadEncPasswordsButton;
	private JButton clearEncPasswordsButton;
	private JButton loadDecKeysButton;
	private JButton clearDecKeysButton;
	private JButton goDecryptButton;
	private JFileChooser fileChooser;
	private JProgressBar progressBar;
	private static final Logger LOG = LoggerFactory.getLogger(AmqOpsTab.class);
	
	private DecryptionWorker decryptionWorker;
	
	public String getTitle() {
		return title;
	}
	
	
	public AmqOpsTab(JmsConfigTab jmsConfigTab) throws JmsDiggerException {
		this.jmsConfigTab = jmsConfigTab;
		this.title = "ActiveMQ Ops";
		jmsConfig = this.jmsConfigTab.getJmsConfig();
		initialContext = jmsConfig.buildInitialContext();
		jmsAuthn = new JmsAuthentication(initialContext, jmsConfig.getConnFactName());
		KeyEncPasswdListManipulator keyEncPasswdListManipulator = new KeyEncPasswdListManipulator();

		
		
		GridBagConstraints masterGbc = new GridBagConstraints();
		JPanel masterPanel = new JPanel(new GridBagLayout());
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		
		
		Insets inset = new Insets(space, space, space, space);
		Insets masterInset = new Insets(space * 2 , space * 2 , space * 2 , space * 2 );
		masterGbc.insets = masterInset;
		masterGbc.anchor = GridBagConstraints.LINE_START;
		GridBagConstraints gbc = new GridBagConstraints();
		
		
		
		
		Border createDestBorder = BorderFactory.createTitledBorder(border, "Create Destination");
		JPanel createDestPanel = new JPanel(new GridBagLayout());
		gbc.insets = inset;
		JLabel destNameLabel = new JLabel("Name");
		gbc.gridx = 0;
		gbc.gridy = 0;
		createDestPanel.add(destNameLabel, gbc);
		
		destNameInput = new JTextField(15);
		gbc.gridx = 1;
		gbc.gridy = 0;
		createDestPanel.add(destNameInput, gbc);
		
		JPanel queueTopicRadioPanel = new JPanel();
		queueRadio = new JRadioButton("Queue");
		queueRadio.setSelected(true);
		topicRadio = new JRadioButton("Topic");
		radioGroup = new ButtonGroup();
		radioGroup.add(queueRadio);
		radioGroup.add(topicRadio);
		queueTopicRadioPanel.add(queueRadio);
		queueTopicRadioPanel.add(topicRadio);
		queueTopicRadioPanel.setBorder(border);
		gbc.gridx = 2;
		gbc.gridy = 0;
		createDestPanel.add(queueTopicRadioPanel, gbc);
		
		createDestButton = new JButton("Go");
		gbc.gridx = 3;
		gbc.gridy = 0;
		createDestPanel.add(createDestButton, gbc);
		createDestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(JmsHelper.isStringNullOrEmpty(destNameInput.getText())) {
					JOptionPane.showMessageDialog(null, "Destination name cannot be empty or null", "Failure", JOptionPane.ERROR_MESSAGE);
					return;
				}
				boolean createQueue = false;
				
				if(queueRadio.isSelected()) {
					createQueue = true;
				}
				try {
					CreateDestinationWorker worker = new CreateDestinationWorker(destNameInput.getText(), createQueue);
					worker.execute();
					createDestButton.setEnabled(false);
				} catch (JmsDiggerException e) {
					createDestButton.setEnabled(true);
					JOptionPane.showMessageDialog(null, "An error has occured. See the results box for details",  "Failure", JOptionPane.ERROR_MESSAGE);
					outputArea.setText(JmsHelper.exceptionStacktraceToString(e));
					return;
				}
			}
		});
		createDestPanel.setBorder(createDestBorder);
		
		masterGbc.insets = masterInset;
		masterGbc.gridx = 0;
		masterGbc.gridy = 0;
		masterPanel.add(createDestPanel, masterGbc);

		
		
		
		Border obtainStatsBorder = BorderFactory.createTitledBorder(border, "Query Statistics");
		JPanel obtainStatsPanel = new JPanel(new GridBagLayout());
		JLabel queryStatsLabel = new JLabel("Name");
		gbc.insets = inset;
		gbc.gridx = 0;
		gbc.gridy = 0;
		obtainStatsPanel.add(queryStatsLabel, gbc);
		
		obtainStatsInput = new JTextField(15);
		gbc.gridx = 1;
		gbc.gridy = 0;
		obtainStatsPanel.add(obtainStatsInput, gbc);
		
		JPanel getStatsRadioPanel = new JPanel();
		queueStats = new JRadioButton("Queue");
		queueStats.setSelected(true);
		topicStats = new JRadioButton("Topic");
		brokerStats = new JRadioButton("Broker");
		subscriptionStats = new JRadioButton("Subscription");
		
		ButtonGroup statsRadioGroup = new ButtonGroup();
		statsRadioGroup.add(queueStats);
		statsRadioGroup.add(topicStats);
		statsRadioGroup.add(brokerStats);
		statsRadioGroup.add(subscriptionStats);
		
		getStatsRadioPanel.add(queueStats);
		getStatsRadioPanel.add(topicStats);
		getStatsRadioPanel.add(brokerStats);
		getStatsRadioPanel.add(subscriptionStats);
		
		getStatsRadioPanel.setBorder(border);
		gbc.gridx = 2;
		gbc.gridy = 0;
		obtainStatsPanel.add(getStatsRadioPanel, gbc);
		
		statsButton = new JButton("Go");
		statsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JmsDestination destType = JmsDestination.QUEUE;
				if((queueStats.isSelected() || topicStats.isSelected()) && JmsHelper.isStringNullOrEmpty(obtainStatsInput.getText())) {
					JOptionPane.showMessageDialog(null, "Destination name cannot be empty for Queue or Topic Statistics", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(queueStats.isSelected()) {
					destType = JmsDestination.QUEUE;
				} else {
					if(topicStats.isSelected()){
						destType = JmsDestination.TOPIC;
					} else {
						if(brokerStats.isSelected()) { 
							destType = JmsDestination.BROKER;
						} else {
							if(subscriptionStats.isSelected()) {
								destType = JmsDestination.SUBSCRIPTION;
							}
						}
						
					}
				}
				
				try {
					QueryDestinationWorker worker = new QueryDestinationWorker(obtainStatsInput.getText(), destType);
					worker.execute();
					statsButton.setEnabled(false);
				} catch (JmsDiggerException e) {
					statsButton.setEnabled(true);
					LOG.info("A JMSDiggerException occured", e);
					JOptionPane.showMessageDialog(null, "An error has occured. See the results box for details",  "Error", JOptionPane.ERROR_MESSAGE);
					outputArea.setText(JmsHelper.exceptionStacktraceToString(e));
					return;
				}
			}
		});
		
		gbc.gridx = 3;
		gbc.gridy = 0;
		obtainStatsPanel.add(statsButton, gbc);
		obtainStatsPanel.setBorder(obtainStatsBorder);
		
		masterGbc.insets = masterInset;
		masterGbc.gridx = 0;
		masterGbc.gridy = 1;
		masterPanel.add(obtainStatsPanel, masterGbc);
		
		
		
		Border decryptBorder = BorderFactory.createTitledBorder(border, "ActiveMQ Password Decryption");

		JPanel passwordDecryptPanel = new JPanel(new GridBagLayout());
		Dimension encryptedAndPasswordListSize = new Dimension(300,80);
		gbc = new GridBagConstraints();
		//gbc.insets = inset;
		
		/*
		 * Build the USERNAME, Jlist and corresponding load and clear buttons
		 */
		JPanel encPasswordListPanel = new JPanel(new GridBagLayout());
		JLabel encPasswordListLabel = new JLabel("Encrypted Passwords");
		gbc.gridx = 0;
		gbc.gridy = 0;
		encPasswordListPanel.add(encPasswordListLabel, gbc);
		
		encPasswordList = new JList<String>();
		encPasswordList.setBorder(border);
		gbc.gridx = 0;
		gbc.gridy = 1;
		JScrollPane encPasswordScrollPane = new JScrollPane(encPasswordList);
		encPasswordScrollPane.setPreferredSize(encryptedAndPasswordListSize);
		encPasswordListPanel.add(encPasswordScrollPane, gbc);
		
		JPanel encPasswordLoadClearPanel = new JPanel();
		loadEncPasswordsButton = new JButton("Load");
		loadEncPasswordsButton.addActionListener(keyEncPasswdListManipulator);
		encPasswordLoadClearPanel.add(loadEncPasswordsButton);
		clearEncPasswordsButton = new JButton("Clear");
		clearEncPasswordsButton.addActionListener(keyEncPasswdListManipulator);
		encPasswordLoadClearPanel.add(clearEncPasswordsButton);
		gbc.gridx = 0;
		gbc.gridy = 2;
		encPasswordListPanel.add(encPasswordLoadClearPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		passwordDecryptPanel.add(encPasswordListPanel, gbc);
		
		/*
		 * Build the PASSWORD, Jlist and corresponding load and clear buttons
		 */
		JPanel decKeyPanel = new JPanel(new GridBagLayout());
		JLabel decKeyLabel = new JLabel("Decryption String");
		gbc.gridx = 0;
		gbc.gridy = 0;
		decKeyPanel.add(decKeyLabel, gbc);
		
		decKeysList = new JList<String>();
		decKeysList.setBorder(border);
		gbc.gridx = 0;
		gbc.gridy = 1;
		JScrollPane decKeyScrollPane = new JScrollPane(decKeysList);
		decKeyScrollPane.setPreferredSize(encryptedAndPasswordListSize);
		decKeyPanel.add(decKeyScrollPane, gbc);
		
		JPanel decKeysLoadClearPanel = new JPanel();
		loadDecKeysButton = new JButton("Load");
		loadDecKeysButton.addActionListener(keyEncPasswdListManipulator);
		decKeysLoadClearPanel.add(loadDecKeysButton);
		clearDecKeysButton = new JButton("Clear");
		clearDecKeysButton.addActionListener(keyEncPasswdListManipulator);
		decKeysLoadClearPanel.add(clearDecKeysButton);
		gbc.gridx = 0;
		gbc.gridy = 2;
		decKeyPanel.add(decKeysLoadClearPanel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		passwordDecryptPanel.add(decKeyPanel, gbc);
		
		
		/*
		 * Create the go button and stick it to the end of the Fuzzing
		 */
		JPanel goDecryptPanel = new JPanel(new GridBagLayout());
		goDecryptButton = new JButton("Go");
		goDecryptButton.addActionListener(new TriggerDecryptionWorker());
		goDecryptPanel.add(goDecryptButton);
		gbc.gridx = 0;
		gbc.gridy = 0;
		goDecryptPanel.add(goDecryptButton, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		passwordDecryptPanel.add(goDecryptPanel, gbc);		
		passwordDecryptPanel.setBorder(decryptBorder);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 2;
		masterPanel.add(passwordDecryptPanel, masterGbc);

		
		/*
		 * Add a text box to display results and exception details
		 */
		JPanel outputBoxPanel = new JPanel(new GridBagLayout());
		outputArea = new JTextArea(9, 55);
		outputArea.setEditable(false);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		outputBoxPanel.add(new JScrollPane(outputArea), gbc);
		Border authBorder = BorderFactory.createTitledBorder(border, "Results");
		outputBoxPanel.setBorder(authBorder);
		masterGbc.gridx = 0;
		masterGbc.gridy = 3;
		masterPanel.add(outputBoxPanel, masterGbc);
		
		
		/*
		 * Pane to display progress
		 */
		JPanel progressPanel = new JPanel(new GridBagLayout());
		JLabel progressLabel = new JLabel("Progress > ");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		progressPanel.add(progressLabel, gbc);

		
		progressBar = new JProgressBar();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		progressPanel.add(progressBar, gbc);
		masterGbc.gridx = 0;
		masterGbc.gridy = 4;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		masterPanel.add(progressPanel, masterGbc);
		
		
		
		masterGbc = new GridBagConstraints();
		masterGbc.gridx = 0;
		masterGbc.gridy = 0;
		this.add(masterPanel, masterGbc);


	}
	
	
	class CreateDestinationWorker extends SwingWorker<ProgressInfo, ProgressInfo> {
		private AMQOps amqOps;
		private boolean createQueue;
		private String destinationName;
		private InitialContext localInitialContext;
		private JmsConfig localJmsConfig; // To ensure that the affects of executing this worker are limited its own objects.
		
		public CreateDestinationWorker(String destinationName, boolean createQueue) throws JmsDiggerException {
			localJmsConfig = jmsConfigTab.getJmsConfig();
			localInitialContext = localJmsConfig.buildInitialContext();
			this.createQueue = createQueue;
			this.destinationName = destinationName;
		}

		@Override
		protected ProgressInfo doInBackground() {
			amqOps = new AMQOps(localInitialContext, localJmsConfig.getConnFactName(), localJmsConfig.getLoginInfo());

			ProgressInfo pInfo = new ProgressInfo();
			try {
				amqOps.init();
				if(createQueue)
					amqOps.createQueue(destinationName);
				else
					amqOps.createTopic(destinationName);
				pInfo.setSuccessful(true);
			} catch (JmsDiggerException e) {
				LOG.info("An error occured while creating a destination", e);
				pInfo.setDescription(JmsHelper.exceptionStacktraceToString(e));
				pInfo.setSuccessful(false);
			}
			return pInfo;
		}
				
		@Override
		public void done() {
			ProgressInfo pInfo = null;
			try {
				pInfo = get();
			} catch (InterruptedException e) {
				LOG.info("An InterruptedException occured", e);
			} catch (ExecutionException e) {
				LOG.info("An ExecutionException occured", e);
			}
			if(pInfo != null) {
				if(pInfo.isSuccessful()) {
					JOptionPane.showMessageDialog(null, "Destination " +destinationName + " created!", "Success", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "Destination " +destinationName + " could not be created!", "Failure", JOptionPane.ERROR_MESSAGE);
					outputArea.setText(pInfo.getDescription());
				}
			}
			createDestButton.setEnabled(true);
		}		
	}
	
	class QueryDestinationWorker extends SwingWorker<ProgressInfo, ProgressInfo> {
		private AMQOps amqOps;
		private JmsDestination destType;
		private String destinationName;
		private InitialContext localInitialContext;
		private JmsConfig localJmsConfig; // To ensure that the affects of executing this worker are limited its own objects.
		
		public QueryDestinationWorker(String destinationName, JmsDestination destType) throws JmsDiggerException {
			localJmsConfig = jmsConfigTab.getJmsConfig();
			localInitialContext = localJmsConfig.buildInitialContext();
			this.destType = destType;
			this.destinationName = destinationName;
		}

		@Override
		protected ProgressInfo doInBackground() {
			amqOps = new AMQOps(localInitialContext, localJmsConfig.getConnFactName(), localJmsConfig.getLoginInfo());

			ProgressInfo pInfo = new ProgressInfo();
			try {
				amqOps.init();
				switch(destType) {
				case QUEUE:
					amqOps.getQueueStats(destinationName, true);
					break;
				case TOPIC:
					amqOps.getTopicStats(destinationName, true);
					break;
				case BROKER:
					amqOps.getBrokerStats();
					break;
				case SUBSCRIPTION:
					amqOps.getSubscriptionsStats();
					break;
				default:
					amqOps.getBrokerStats();
					break;
				}
				pInfo.setDescription(amqOps.getStatsAsString());
				pInfo.setSuccessful(true);
			} catch (JmsDiggerException e) {
				LOG.info("A JMSDiggerException has occured", e);
				pInfo.setDescription(JmsHelper.exceptionStacktraceToString(e));
				pInfo.setSuccessful(false);
			}
			return pInfo;
		}
				
		@Override
		public void done() {
			ProgressInfo pInfo = null;
			try {
				pInfo = get();
			} catch (InterruptedException e) {
				LOG.info("Error occured while retrieving thread results", e);
				JmsGuiCommon.showErrorAndLogMessage(e.getMessage());
			} catch (ExecutionException e) {
				LOG.info("Error occured while retrieving thread results", e);
				JmsGuiCommon.showErrorAndLogMessage(e.getMessage());
			}
			if(pInfo != null) {
				if(pInfo.isSuccessful()) {
					//JOptionPane.showMessageDialog(null, "Destination " +destinationName + " created!", "Success", JOptionPane.INFORMATION_MESSAGE);
					outputArea.setText(pInfo.getDescription());
				} else {
					JOptionPane.showMessageDialog(null, destinationName + " could not be queried!", "Failure", JOptionPane.ERROR_MESSAGE);
					outputArea.setText(pInfo.getDescription());
				}
			}
			statsButton.setEnabled(true);
		}		
	}
	
	class KeyEncPasswdListManipulator implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			DefaultListModel<String> decKeysAndEncPasswdListModel = new DefaultListModel<String>();
			if(event.getSource() == clearDecKeysButton) {
				decKeysList.setModel(decKeysAndEncPasswdListModel);
				return;
			}
			else {
				if(event.getSource() == clearEncPasswordsButton) {
					encPasswordList.setModel(decKeysAndEncPasswdListModel);
					return;
				}
			}
					
			
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			Scanner fileScanner;

			int result = fileChooser.showOpenDialog(null);
			if(result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				try {
					fileScanner = new Scanner(new FileInputStream(file));
					while(fileScanner.hasNextLine()) {
						decKeysAndEncPasswdListModel.addElement(fileScanner.nextLine());
					}
					fileScanner.close();
				} catch (FileNotFoundException e) {
					LOG.info("An error occured with the Scanner", e);
					JmsGuiCommon.showErrorAndLogMessage("An error occured with the Scanner.");
				}
				if(event.getSource() == loadDecKeysButton)
					decKeysList.setModel(decKeysAndEncPasswdListModel);
				else 
					if(event.getSource() == loadEncPasswordsButton)
						encPasswordList.setModel(decKeysAndEncPasswdListModel);
			}
		}
	}
	
	
	class TriggerDecryptionWorker implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			outputArea.setText("");
			progressBar.setValue(0);
			DefaultListModel<String> decKeysModel = (DefaultListModel<String>)(decKeysList.getModel());
			ArrayList<String> decKeysArrayList = new ArrayList<String>();

			DefaultListModel<String> encPasswordsModel = (DefaultListModel<String>)(encPasswordList.getModel());
			ArrayList<String> encPasswordsArrayList = new ArrayList<String>();

			
			
			if(decKeysModel.getSize() == 0 || encPasswordsModel.getSize() == 0) {
				JOptionPane.showMessageDialog(null, "Decryption Keys or Passwords not available", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			Enumeration<String> e = encPasswordsModel.elements();
			while(e.hasMoreElements())
				encPasswordsArrayList.add(e.nextElement());
			
			e = decKeysModel.elements();
			while(e.hasMoreElements())
				decKeysArrayList.add(e.nextElement());
			
			decryptionWorker = new DecryptionWorker(encPasswordsArrayList, decKeysArrayList);
			decryptionWorker.execute();
			disableAllButtons();
		}
		
		private void disableAllButtons() {
			loadEncPasswordsButton.setEnabled(false);
			clearEncPasswordsButton.setEnabled(false);
			loadDecKeysButton.setEnabled(false);
			clearDecKeysButton.setEnabled(false);
			goDecryptButton.setEnabled(false);
		}
	}
		
	/**
	 * One million decryption keys (passwords) against one password took 260 seconds
	 * @author Gursev Singh Kalra
	 *
	 */
	class DecryptionWorker extends SwingWorker<String, ProgressInfo> {

		private ArrayList<String> encPasswordsArrayList;
		private ArrayList<String> decKeysArrayList;
		
		public DecryptionWorker(ArrayList<String> encPasswordsArrayList, ArrayList<String> decKeysArrayList) {
			this.encPasswordsArrayList = encPasswordsArrayList;
			this.decKeysArrayList = decKeysArrayList;
		}
		
		@Override
		public String doInBackground() throws JmsDiggerException {

			JmsPasswordOps passwordDecryptor = new JmsPasswordOps();
			StringBuilder testResults;
			StringBuilder workingCreds = new StringBuilder();
			String decryptedPassword = null;
			workingCreds.append("\n\n###########################################################\nSuccessful Decryption:\n");
			int encPasswordDecKeyCombinations = encPasswordsArrayList.size() * decKeysArrayList.size();
			int count = 0;
			for(String encPassword: encPasswordsArrayList) {
				for(String decKey: decKeysArrayList) {
					testResults = new StringBuilder();
					
					testResults.append("Trying => \""
										+ encPassword + 
										"\" with \"" +
										decKey + "\" : Result => ");
					count++;

					decryptedPassword = passwordDecryptor.decryptOne(encPassword, decKey); 
					if(decryptedPassword == null) {
						testResults.append("Failed");
						publish(new ProgressInfo(testResults.toString(), false, count*100/encPasswordDecKeyCombinations));
					} else {
						testResults.append("Success");
						publish(new ProgressInfo(testResults.toString(), true, count*100/encPasswordDecKeyCombinations));
						workingCreds.append("\nEncrypted Password: \"" + encPassword+
											"\", Decryption Key: \""+ decKey +
											"\", Decrypted Password: \"" + decryptedPassword + "\"");
					}
					
				}
			}			
			return workingCreds.toString();
		} 
		
		@Override
		public void process(List<ProgressInfo> progressInfo) {
			progressBar.setValue(progressInfo.get(progressInfo.size() - 1).getPercentage());
			for(ProgressInfo pInfo : progressInfo)
				outputArea.append("\n" + pInfo.getDescription());
		}
		
		@Override 
		public void done() {
			try {
				outputArea.append(get());
			} catch (InterruptedException e) {
				LOG.info("Thread was interrupted", e);
			} catch (ExecutionException e) {
				LOG.info("An executionException occured", e);
			}
			enableAllButtons();
		}
		
		private void enableAllButtons() {
			loadEncPasswordsButton.setEnabled(true);
			clearEncPasswordsButton.setEnabled(true);
			loadDecKeysButton.setEnabled(true);
			clearDecKeysButton.setEnabled(true);
			goDecryptButton.setEnabled(true);
		}
	
	}
}
