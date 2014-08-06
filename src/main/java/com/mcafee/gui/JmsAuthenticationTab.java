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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcafee.JmsAuthentication;
import com.mcafee.JmsDiggerException;
import com.mcafee.JmsHelper;
import com.mcafee.JmsLoginInfo;

/**
 * 
 * @author Gursev Singh Kalra  @ McAfee, Inc.
 *
 */
public class JmsAuthenticationTab extends JPanel {

	private static final Logger LOG = LoggerFactory.getLogger(JmsAuthenticationTab.class);
	private static int space = 5;
	private String title;
	private JFileChooser fileChooser;
	
	public String getTitle() {
		return title;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField singleUserInput ;
	private JTextField singlePassInput;
	private JButton goSingleUser;
	private JList<String> usernameList;
	private JButton loadUserIdsButton;
	private JButton clearUserIdsButton;
	private JList<String> passwordList;
	private JButton loadPasswordsButton;
	private JButton clearPasswordsButton;
	private JButton goBruteforceButton;
	private JTextArea outputArea;
	private JProgressBar progressBar;
	private JmsConfigTab jmsConfigTab;
	private AuthenticationWorker authenticationWorker;

	
	public JmsAuthenticationTab(JmsConfigTab jmsConfigTab) {
		this.jmsConfigTab = jmsConfigTab;
		UsernamePasswordListManipulator usernamePasswordListManipulatorObject = new UsernamePasswordListManipulator();
		TriggerAuthenticationWorker triggerAuthenticationWorkerObject = new TriggerAuthenticationWorker();
		
		JLabel blankLabel = new JLabel("\n");
		Insets inset = new Insets(space, space, space, space);
		Insets masterInset = new Insets(space * 2, space *2 , space*2, space*2);
		GridBagLayout masterGbl = new GridBagLayout();
		GridBagConstraints masterGbc = new GridBagConstraints();
		this.setLayout(masterGbl);
		this.title = "Authentication";
		
		JPanel singleUserPanel = new JPanel(new GridBagLayout());		
		JLabel singleUserLabel = new JLabel("Username: ");
		singleUserLabel.setHorizontalAlignment(JLabel.RIGHT);
		JLabel singlePassLabel = new JLabel("Password: ");
		singlePassLabel.setHorizontalAlignment(JLabel.RIGHT);
		singlePassInput = new JTextField(20);
		goSingleUser = new JButton("Go");
		goSingleUser.addActionListener(triggerAuthenticationWorkerObject);
		GridBagConstraints gbc = new GridBagConstraints();
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border singleUserBorder = BorderFactory.createTitledBorder(border, "Credential Check");
		Border fuzzBorder = BorderFactory.createTitledBorder(border, "Credential Brute Force");
		

		gbc.insets = inset;
		gbc.gridx = 0;
		gbc.gridy = 0;
		singleUserPanel.add(singleUserLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		singleUserInput = new JTextField(20);
		//gbc.weightx = 1.0;
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		singleUserPanel.add(singleUserInput, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		singleUserPanel.add(singlePassLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		//gbc.weightx = 1.0;
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		singleUserPanel.add(singlePassInput, gbc);
		
		
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(goSingleUser);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		singleUserPanel.add(buttonPanel, gbc);
		singleUserPanel.setBorder(singleUserBorder);

		masterGbc.gridx = 0;
		masterGbc.gridy = 0;
		masterGbc.anchor = GridBagConstraints.LINE_START;
		this.add(singleUserPanel, masterGbc);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 1;
		this.add(blankLabel, masterGbc);
		
		
		
		/*
		 * Panel to accomodate controls for username and password lists
		 */
		JPanel loginFuzzPanel = new JPanel(new GridBagLayout());
		Dimension userPasswdListSize = new Dimension(275,70);
		gbc = new GridBagConstraints();
		//gbc.insets = inset;
		
		/*
		 * Build the USERNAME, Jlist and corresponding load and clear buttons
		 */
		JPanel usernamePanel = new JPanel(new GridBagLayout());
		JLabel userIdLabel = new JLabel("Usernames");
		gbc.gridx = 0;
		gbc.gridy = 0;
		usernamePanel.add(userIdLabel, gbc);
		
		usernameList = new JList<String>();
		DefaultListModel<String> usernameModel = new DefaultListModel<String>();
		usernameList.setModel(usernameModel);
		usernameList.setBorder(border);
		gbc.gridx = 0;
		gbc.gridy = 1;
		JScrollPane userPane = new JScrollPane(usernameList);
		userPane.setPreferredSize(userPasswdListSize);
		usernamePanel.add(userPane, gbc);
		
		JPanel usernameLoadClearPanel = new JPanel();
		loadUserIdsButton = new JButton("Load");
		usernameLoadClearPanel.add(loadUserIdsButton);
		loadUserIdsButton.addActionListener(usernamePasswordListManipulatorObject);
		clearUserIdsButton = new JButton("Clear");
		usernameLoadClearPanel.add(clearUserIdsButton);
		clearUserIdsButton.addActionListener(usernamePasswordListManipulatorObject);
		gbc.gridx = 0;
		gbc.gridy = 2;
		usernamePanel.add(usernameLoadClearPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		loginFuzzPanel.add(usernamePanel, gbc);
		
		/*
		 * Build the PASSWORD, Jlist and corresponding load and clear buttons
		 */
		JPanel passwordPanel = new JPanel(new GridBagLayout());
		JLabel passwordsLabel = new JLabel("Passwords");
		gbc.gridx = 0;
		gbc.gridy = 0;
		passwordPanel.add(passwordsLabel, gbc);
		
		passwordList = new JList<String>();
		DefaultListModel<String> passwordModel = new DefaultListModel<String>();
		passwordList.setModel(passwordModel);
		//passwordList.setPreferredSize(userPasswdListSize);
		passwordList.setBorder(border);
		gbc.gridx = 0;
		gbc.gridy = 1;
		JScrollPane passPane = new JScrollPane(passwordList);
		passPane.setPreferredSize(userPasswdListSize);
		passwordPanel.add(passPane, gbc);
		
		
		/**
		 * CODE BEGIN - Load and clear passwords
		 */
		JPanel passwordLoadClearPanel = new JPanel();
		loadPasswordsButton = new JButton("Load");
		passwordLoadClearPanel.add(loadPasswordsButton);
		loadPasswordsButton.addActionListener(usernamePasswordListManipulatorObject);
		clearPasswordsButton = new JButton("Clear");
		passwordLoadClearPanel.add(clearPasswordsButton);
		clearPasswordsButton.addActionListener(usernamePasswordListManipulatorObject);
		gbc.gridx = 0;
		gbc.gridy = 2;
		passwordPanel.add(passwordLoadClearPanel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		loginFuzzPanel.add(passwordPanel, gbc);
		/**
		 * CODE ENDS - Load and clear passwords
		 */
		
		
		/*
		 * Create the go button and stick it to the end of the Fuzzing
		 */
		JPanel goBruteforcePanel = new JPanel(new GridBagLayout());
		goBruteforceButton = new JButton("Go");
		goBruteforceButton.addActionListener(triggerAuthenticationWorkerObject);
		goBruteforcePanel.add(goBruteforceButton);
		gbc.gridx = 0;
		gbc.gridy = 0;
		goBruteforcePanel.add(goBruteforceButton, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		loginFuzzPanel.add(goBruteforcePanel, gbc);
		
		masterGbc.gridx = 0;
		masterGbc.gridy = 2;
		loginFuzzPanel.setBorder(fuzzBorder);
		this.add(loginFuzzPanel, masterGbc);

		
		/*
		 * Add a text box to display results and exception details
		 */
		JPanel outputBoxPanel = new JPanel(new GridBagLayout());
		outputArea = new JTextArea(15, 50);
		outputArea.setEditable(false);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		outputBoxPanel.add(new JScrollPane(outputArea), gbc);
		Border authBorder = BorderFactory.createTitledBorder(border, "Results");
		outputBoxPanel.setBorder(authBorder);
		masterGbc.gridx = 0;
		masterGbc.gridy = 3;
		this.add(outputBoxPanel, masterGbc);
		
		
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
		masterGbc.insets = masterInset;
		this.add(progressPanel, masterGbc);
		
		
	}
	
	class UsernamePasswordListManipulator implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			DefaultListModel<String> usernamePasswordListModel = new DefaultListModel<String>();
			if(event.getSource() == clearUserIdsButton) {
				usernameList.setModel(usernamePasswordListModel);
				return;
			}
			else {
				if(event.getSource() == clearPasswordsButton) {
					passwordList.setModel(usernamePasswordListModel);
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
						usernamePasswordListModel.addElement(fileScanner.nextLine());
					}
					fileScanner.close();
				} catch (FileNotFoundException e) {
					LOG.info("An error has occured with the file Scanner", e);
					JmsGuiCommon.showErrorAndLogMessage("An error occured with the file Scanner.");
				}
				if(event.getSource() == loadUserIdsButton)
					usernameList.setModel(usernamePasswordListModel);
				else 
					if(event.getSource() == loadPasswordsButton)
						passwordList.setModel(usernamePasswordListModel);
			}
		}
	}
	
	class TriggerAuthenticationWorker implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			outputArea.setText("");
			progressBar.setValue(0);
			DefaultListModel<String> userIdModel = (DefaultListModel<String>)(usernameList.getModel());
			DefaultListModel<String> passwordModel = (DefaultListModel<String>)(passwordList.getModel());

			ArrayList<String> usernameArrayList = new ArrayList<String>();
			ArrayList<String> passwordArrayList = new ArrayList<String>();

			if(event.getSource() == goSingleUser) {
				usernameArrayList.add(singleUserInput.getText());
				passwordArrayList.add(singlePassInput.getText());
			} else {
				if(event.getSource() == goBruteforceButton) {
					
					if(passwordModel.getSize() == 0)
						passwordArrayList.add("");
					else {
						Enumeration<String> e = passwordModel.elements();
						while(e.hasMoreElements())
							passwordArrayList.add(e.nextElement());
					}
					
					if(userIdModel.getSize() == 0)
						usernameArrayList.add("");
					else {
						Enumeration<String> e = userIdModel.elements();
						while(e.hasMoreElements())
							usernameArrayList.add(e.nextElement());			
					}
				}
			}
			try {
				authenticationWorker = new AuthenticationWorker(jmsConfigTab.getJmsConfig(), usernameArrayList, passwordArrayList);
			} catch (JmsDiggerException e) {
				LOG.info("An error has occured while creating an AuthenticationWorker", e);
				JmsGuiCommon.showErrorAndLogMessage("An error has occured while creating an AuthenticationWorker");
			}
			authenticationWorker.execute();
			disableAllButtons();
		}
		
		private void disableAllButtons() {
			goSingleUser.setEnabled(false);
			goBruteforceButton.setEnabled(false);
			loadPasswordsButton.setEnabled(false);
			loadUserIdsButton.setEnabled(false);
			clearPasswordsButton.setEnabled(false);
			clearUserIdsButton.setEnabled(false);
		}
	}
	
	class ProgressInfo {
		private String accessResult;
		private int percentage;
		
		public String getAccessResult() {
			return accessResult;
		}

		public int getPercentage() {
			return percentage;
		}

		public ProgressInfo(String accessResult, int percentage) {
			this.accessResult = accessResult;
			this.percentage = percentage;
		}
	}
	
	class AuthenticationWorker extends SwingWorker<String, ProgressInfo> {

		private ArrayList<String> usernameArrayList;
		private ArrayList<String> passwordArrayList;
		
		private JmsConfig jmsConfig;
		private InitialContext initialContext;
		private JmsLoginInfo loginInfo;
		private JmsAuthentication jmsAuthn;
		private boolean boolResult;
		
		public AuthenticationWorker(JmsConfig jmsConfig, ArrayList<String> usernameArrayList, ArrayList<String> passwordArrayList) {
			this.jmsConfig = jmsConfig;
			this.usernameArrayList = usernameArrayList;
			this.passwordArrayList = passwordArrayList;
		}
		
		@Override
		public String doInBackground() throws JmsDiggerException {
			StringBuilder testResults;
			StringBuilder workingCreds = new StringBuilder();
			workingCreds.append("\n\n###########################################################\nWorking Credentials:\n");
//			contextFactory = new JmsInitialContextFactory(jmsConfig.getCtxFactoryClass(), jmsConfig.getProviderUrl());
//			contextFactory.addConnectionFactory(jmsConfig.getConnFactName());
//			ctx = contextFactory.getInitialContext();
			initialContext = jmsConfig.buildInitialContext();
			jmsAuthn = new JmsAuthentication(initialContext, jmsConfig.getConnFactName());
			int userPasswordCombinations = usernameArrayList.size() * passwordArrayList.size();
			int count = 0;
			for(String username: usernameArrayList) {
				for(String password: passwordArrayList) {
					testResults = new StringBuilder();
					loginInfo = new JmsLoginInfo(username, password);
					
					testResults.append("###########################################################\nTrying => "
										+loginInfo 
										+"\n"
										+"Result: ");
					count++;

					boolResult = jmsAuthn.isLoginInfoValid(loginInfo);
					if(boolResult == false) {
						testResults.append("Failed\n");
						testResults.append(JmsHelper.exceptionStacktraceToString(jmsAuthn.getException()));
					} else {
						testResults.append("Success\n");
						workingCreds.append(loginInfo + "\n");
					}
					
					publish(new ProgressInfo(testResults.toString(), count*100/userPasswordCombinations));
				}
			}			
			return workingCreds.toString();
		} 
		
		@Override
		public void process(List<ProgressInfo> progressInfo) {
			progressBar.setValue(progressInfo.get(progressInfo.size() - 1).getPercentage());
			for(ProgressInfo p : progressInfo) {
				outputArea.append("\n" + p.getAccessResult());
			}
		}
		
		@Override 
		public void done() {
			//outputArea.append();
			try {
				outputArea.append(get());
			} catch (InterruptedException e) {
				LOG.info("An error has occured while appending to the outputArea", e);
			} catch (ExecutionException e) {
				LOG.info("An error has occured while appending to the outputArea", e);
			}
			enableAllButtons();
		}
		
		private void enableAllButtons() {
			//Thread.sleep(3* 1000);
			goSingleUser.setEnabled(true);
			goBruteforceButton.setEnabled(true);
			loadPasswordsButton.setEnabled(true);
			loadUserIdsButton.setEnabled(true);
			clearPasswordsButton.setEnabled(true);
			clearUserIdsButton.setEnabled(true);
		}
	
	}

}
