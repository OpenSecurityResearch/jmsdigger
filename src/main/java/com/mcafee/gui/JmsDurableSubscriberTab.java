package com.mcafee.gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.naming.InitialContext;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcafee.JmsDiggerException;
import com.mcafee.JmsDurableSubscriberManipulator;
import com.mcafee.JmsHelper;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsDurableSubscriberTab extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	private int space = 5;
	private JmsConfigTab jmsConfigTab;
	private JTextField topicNameInput;
	private JTextField messageSelectorInput;
	private JSpinner durableSubsCountSpinner;
	private SpinnerNumberModel spinnerModel;
	private JButton createMultipleDurableSubsButton;
	private JProgressBar progressBar;
	private JTextField clientIdInput;
	private JTextField subsNameInput;
	private JButton createSingleSubsButton;
	private JButton eraseDurableSubsButton;
	private JTextArea outputArea;
	private JmsConfig jmsConfig;
	private InitialContext initialContext;
	private JmsDurableSubscriberManipulator manipulator ;
	private static final Logger LOG = LoggerFactory.getLogger(JmsDurableSubscriberTab.class);

	
	public String getTitle() {
		return title;
	}
	
	public JmsDurableSubscriberTab(JmsConfigTab jmsConfigTab) throws JmsDiggerException {
		this.jmsConfigTab = jmsConfigTab;
		//jmsConfig = this.jmsConfigTab.getJmsConfig();
		/**
		 * The following line is commented out to enable late Initial Context generation. 
		 * There are additional two lines of code added see comments beginning __LAZY__ below.
		 * This mostly appears to be a workaround, but i will have to create a more robust library. 
		 */
		//initialContext = jmsConfig.buildInitialContext();
		JPanel masterPanel = new JPanel(new GridBagLayout());
		title = "Durable Subscribers";
		TriggerDurableSubscriberCreation trigger = new TriggerDurableSubscriberCreation();
		
		JPanel nameFilterPanel = new JPanel(new GridBagLayout());
		GridBagConstraints masterGbc = new GridBagConstraints();
		GridBagConstraints gbc = new GridBagConstraints();
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Insets inset = new Insets(space, space, space, space);
		Insets masterInset = new Insets(space * 2, space * 2, space * 2, space * 2);
		masterGbc.insets = masterInset;
		
		gbc.insets = inset;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		JLabel topicNameLabel = new JLabel("Topic Name");
		gbc.gridx = 0;
		gbc.gridy = 0;
		topicNameLabel.setHorizontalAlignment(JLabel.RIGHT);
		nameFilterPanel.add(topicNameLabel, gbc);
		topicNameInput = new JTextField(30);
		gbc.gridx = 1;
		gbc.gridy = 0;
		nameFilterPanel.add(topicNameInput, gbc);

		JLabel filterLabel = new JLabel("Message Selector");
		gbc.gridx = 0;
		gbc.gridy = 1;
		filterLabel.setHorizontalAlignment(JLabel.RIGHT);
		nameFilterPanel.add(filterLabel, gbc);
		messageSelectorInput = new JTextField(30);
		gbc.gridx = 1;
		gbc.gridy = 1;
		nameFilterPanel.add(messageSelectorInput, gbc);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 0;
		masterGbc.anchor = GridBagConstraints.LINE_START;
		masterPanel.add(nameFilterPanel, masterGbc);
		
		
		
		JPanel randomCountPanel = new JPanel(new GridBagLayout());
		Border singleUserBorder = BorderFactory.createTitledBorder(border, "Create Multiple Durable Subscribers");
		
		spinnerModel = new SpinnerNumberModel(10, 1, 9999, 1);
		durableSubsCountSpinner = new JSpinner(spinnerModel);
		gbc = new GridBagConstraints();
		gbc.insets = inset;
		gbc.gridx = 0;
		gbc.gridy = 0;
		randomCountPanel.add(durableSubsCountSpinner, gbc);
		createMultipleDurableSubsButton = new JButton("Create");
		createMultipleDurableSubsButton.addActionListener(trigger);
		gbc.gridx = 1;
		gbc.gridy = 0;
		randomCountPanel.add(createMultipleDurableSubsButton, gbc);
		
		
//		JLabel progressLabel = new JLabel("Progress > ");
//		gbc.fill = GridBagConstraints.HORIZONTAL;
//		gbc.gridx = 0;
//		gbc.gridy = 1;
//		randomCountPanel.add(progressLabel, gbc);
		
//		progressBar = new JProgressBar();
//		progressBar.setBounds(0, 0, 30, 100);
//		
//		gbc.gridx = 1;
//		gbc.gridy = 1;
//		gbc.gridwidth = 2;
//		randomCountPanel.add(progressBar, gbc);
		randomCountPanel.setBorder(singleUserBorder);
		
		masterGbc = new GridBagConstraints();
		masterGbc.gridx = 0;
		masterGbc.gridy = 2;
		masterGbc.insets = inset;
		masterGbc.anchor = GridBagConstraints.LINE_START;
		masterGbc.fill = GridBagConstraints.HORIZONTAL;
		masterPanel.add(randomCountPanel, masterGbc);
		
		
		
		
		
		JPanel singleSubsPanel = new JPanel(new GridBagLayout());
		Border singleSubsBorder = BorderFactory.createTitledBorder(border, "Create/Erase A Durable Subscriber");
		gbc = new GridBagConstraints();
		
		JLabel clientIdLabel = new JLabel("Client ID");
		clientIdLabel.setHorizontalAlignment(JLabel.RIGHT);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = inset;
		gbc.gridx = 0;
		gbc.gridy = 0;
		singleSubsPanel.add(clientIdLabel, gbc);
		
		clientIdInput = new JTextField(30);
		gbc.gridx = 1;
		gbc.gridy = 0;
		singleSubsPanel.add(clientIdInput, gbc);
		
		JLabel subsNameLabel = new JLabel("Durable Subscriber Name");
		subsNameLabel.setHorizontalAlignment(JLabel.RIGHT);
		gbc.insets = inset;
		gbc.gridx = 0;
		gbc.gridy = 1;
		singleSubsPanel.add(subsNameLabel, gbc);
		
		subsNameInput = new JTextField(30);
		gbc.gridx = 1;
		gbc.gridy = 1;
		singleSubsPanel.add(subsNameInput, gbc);
		
		JPanel createButtonPanel = new JPanel(new GridBagLayout()); 
		createSingleSubsButton = new JButton("Create");
		createSingleSubsButton.addActionListener(trigger);
		gbc.gridx = 0;
		gbc.gridy = 0;
		createButtonPanel.add(createSingleSubsButton, gbc);

		eraseDurableSubsButton = new JButton("Erase");
		eraseDurableSubsButton.addActionListener(trigger);
		gbc.gridx = 1;
		gbc.gridy = 0;
		createButtonPanel.add(eraseDurableSubsButton, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		singleSubsPanel.add(createButtonPanel, gbc);
		singleSubsPanel.setBorder(singleSubsBorder);
		
		JPanel outputBoxPanel = new JPanel(new GridBagLayout());
		outputArea = new JTextArea(15, 50);
		outputArea.setEditable(false);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		outputBoxPanel.add(new JScrollPane(outputArea), gbc);
		Border outputBorder = BorderFactory.createTitledBorder(border, "Results");
		outputBoxPanel.setBorder(outputBorder);
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
		masterGbc.insets = inset;
		masterPanel.add(progressPanel, masterGbc);

		
		masterGbc.gridx = 0;
		masterGbc.gridy = 1;
		masterGbc.anchor = GridBagConstraints.LINE_START;
		masterGbc.insets = inset;
		masterPanel.add(singleSubsPanel, masterGbc);
		
		this.add(masterPanel, masterGbc);
		
	}
	
	class TriggerDurableSubscriberErase implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
		}
		
	}
	
	class TriggerDurableSubscriberCreation implements ActionListener {

		public void disableAllButtons() {
			createMultipleDurableSubsButton.setEnabled(false);
			createSingleSubsButton.setEnabled(false);
			eraseDurableSubsButton.setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent event) {
			DurableSubscriberWorker durableSubscriberWorker;
			if(JmsHelper.isStringNullOrEmpty(topicNameInput.getText())) {
				JOptionPane.showMessageDialog(null, "Topic name cannot be empty or null", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if(event.getSource() == eraseDurableSubsButton) {
				if(JmsHelper.isStringNullOrEmpty(subsNameInput.getText()) || JmsHelper.isStringNullOrEmpty(clientIdInput.getText())) {
					JOptionPane.showMessageDialog(null, "Erase function requires a Client ID and Durable Subscriber Name", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			String topicName = topicNameInput.getText();
			String messageSelector = messageSelectorInput.getText();
			int count = 1;
			String clientId = clientIdInput.getText();
			String subsName = subsNameInput.getText();
			
			ArrayList<String> durableSubscriberNames = new ArrayList<String>(); 
			if(JmsHelper.isStringNullOrEmpty(clientId)) {
				clientId = JmsHelper.getRandomString();
			}
			
			if(event.getSource() == createSingleSubsButton) {				
				if(JmsHelper.isStringNullOrEmpty(subsName)) {
					durableSubscriberNames.add(JmsHelper.getRandomString());
				}
				else {
					durableSubscriberNames.add(subsName);
				}
 
			} else {
				if(event.getSource() == createMultipleDurableSubsButton) {
					count = (Integer) spinnerModel.getValue();
					for(int i = 0; i < count; i++) {
						durableSubscriberNames.add(JmsHelper.getRandomString());
					}
				} else if(event.getSource() == eraseDurableSubsButton) {
					durableSubscriberNames.add(subsName);
				}
			}
			
			disableAllButtons();
			//durableSubscriberWorker = new DurableSubscriberWorker(topicName, messageSelector, clientIdList, durableSubscriberNames, (Integer) spinnerModel.getValue());
			try {
				durableSubscriberWorker = new DurableSubscriberWorker(topicName, messageSelector, clientId, durableSubscriberNames, count);
				if(event.getSource() == eraseDurableSubsButton) {
					durableSubscriberWorker.setErase(true);
				}
				durableSubscriberWorker.execute();
			} catch (JmsDiggerException e) {
				LOG.info("An error has occured while creating/executing a durableSubscriberWorker", e);
				JmsGuiCommon.showErrorAndLogMessage(e.getMessage());
			}
		}
		
	}
	
	class DurableSubscriberWorker extends SwingWorker<ProgressInfo, ProgressInfo> {
		
		String messageSelector;
		String topicName;
		ArrayList<String> clientIdList;
		ArrayList<String> durableSubscriberNames;
		String clientId;
		int count;
		boolean erase;
		
		public void setErase(boolean erase) {
			this.erase = erase;
		}

		//public DurableSubscriberWorker(String topicName, String messageSelector, ArrayList<String> clientIdList, ArrayList<String> durableSubscriberNames, int count) {
		public DurableSubscriberWorker(String topicName, String messageSelector, String clientId, ArrayList<String> durableSubscriberNames, int count) throws JmsDiggerException {
			this.topicName = topicName;
			this.messageSelector = messageSelector;
			//this.clientIdList = clientIdList;
			this.clientId = clientId;
			this.durableSubscriberNames = durableSubscriberNames;
			this.count = count;
			this.erase = false;
			
			/**
			 * __LAZY__ the addTopic call adds the topic to the initial context's environment
			 * variables before building the initial context. Once initial context is built, it cannot be changed. <br/>
			 * Initializing the jmsConfig here to ensure that a new configuration is generated for each button click 
			 * to accommodate for topic changes for a single run.  
			 */
			jmsConfig = jmsConfigTab.getJmsConfig();
			jmsConfig.addTopic(topicName);
			initialContext = jmsConfig.buildInitialContext();
		}
		
		public boolean isErase() {
			return erase;
		}
		
		@Override
		protected ProgressInfo doInBackground() throws Exception {
			
			try {
				manipulator = new JmsDurableSubscriberManipulator(initialContext, topicName, jmsConfig.getConnFactName(), jmsConfig.getLoginInfo());
				manipulator.init(clientId);
			} catch (JmsDiggerException ex) {
				LOG.info("DurableSubscriberManipulator initialization failed", ex);
				ProgressInfo i = new ProgressInfo(JmsHelper.exceptionStacktraceToString(ex), false, 0);
				return i;
			}
			
			//publish(new ProgressInfo("\n==================================================\n[+] Creating durable subscribers with clientID \"" + clientId+"\"" , true, 0));
			try {
				for(int i = 0; i < durableSubscriberNames.size() ; i++) {
					try {
						if(!isErase()) {
							manipulator.createDurableSubscriber(durableSubscriberNames.get(i), messageSelector);
							publish(new ProgressInfo("\n[+] Created durable subscriber \"" + durableSubscriberNames.get(i) +"\"" ,true, i*100/count));
						} else {
							manipulator.eraseDurableSubscriber(durableSubscriberNames.get(0));
							publish(new ProgressInfo("\n[+] Erased durable subscriber \"" + durableSubscriberNames.get(i) +"\"" ,true, 100));
						}
					} catch(JmsDiggerException ex) {
						if(!isErase())
							publish(new ProgressInfo("\n[-] Could not create durable subscriber \"" + durableSubscriberNames.get(i) +"\"" ,true, i*100/count));
						else
							publish(new ProgressInfo("\n[-] Could not erase durable subscriber \"" + durableSubscriberNames.get(i) +"\"" ,true, 100));
					} 
				}
			} finally {
				/**
				 * Very important to close the connections as ActiveMQ allows only one active connection from one clientID.
				 * If the connection is not closed, ActiveMQ throws an exception of the following type
				 * Caused by: javax.jms.InvalidClientIDException: Broker: localhost - Client: <clientID> already connected from tcp://ipadderss:port  
				 */			
				manipulator.close(); 
			}
		
			
			return new ProgressInfo("Completed", true, 100);
		}
		
		@Override
		public void process(List<ProgressInfo> progressInfo) {
			for(ProgressInfo pInfo : progressInfo) {
				outputArea.append(pInfo.getDescription());
			}
			ProgressInfo pInfo = progressInfo.get(progressInfo.size()-1);
			progressBar.setValue(pInfo.getPercentage());
		}
		
		
		@Override
		public void done() {
			try {
				ProgressInfo pInfo = get();
				if(pInfo.isSuccessful() == false && pInfo.getPercentage() == 0) {
					outputArea.setText(pInfo.getDescription());
					JOptionPane.showMessageDialog(null, "An error has occured while creating durable subscribers", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					progressBar.setValue(pInfo.getPercentage());
				}
			} catch (InterruptedException ex) {
				LOG.info("", ex);
				JmsGuiCommon.showErrorAndLogMessage(ex.getMessage());
			} catch (ExecutionException ex) {
				LOG.info("", ex);
				JmsGuiCommon.showErrorAndLogMessage(ex.getMessage());
			}
			JOptionPane.showMessageDialog(null, "Done!", "", JOptionPane.INFORMATION_MESSAGE);
			enableAllButtons();
			
		}
				
		public void enableAllButtons() {
			createMultipleDurableSubsButton.setEnabled(true);
			createSingleSubsButton.setEnabled(true);
			eraseDurableSubsButton.setEnabled(true);
		}
	}
}
