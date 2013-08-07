package com.mcafee.gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.naming.InitialContext;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcafee.JmsAuthentication;
import com.mcafee.JmsDiggerException;
import com.mcafee.JmsHelper;
import com.mcafee.JmsInitialContextFactory;
import com.mcafee.JmsLoginInfo;

/**
 * This panel contains all the configuration related items
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */

public class JmsConfigTab extends JPanel {

	/**
	 * 
	 */
	private static final Logger LOG = LoggerFactory.getLogger(JmsConfigTab.class);
	private static final long serialVersionUID = 1L;
	private String title;
	private static int space = 5;
	
	private JTextField ctxFactoryClassInput;
	private JTextField providerUrlInput;
	private JTextField usernameInput;
	private JPasswordField passwordInput;
	private JTextField connFactNameInput;
	private JButton testConfigButton;

	
	public String getTitle() {
		return title;
	}
	
	public JmsConfig getJmsConfig() throws JmsDiggerException {
		if(JmsHelper.isStringNullOrEmpty(connFactNameInput.getText()) 
				|| JmsHelper.isStringNullOrEmpty(ctxFactoryClassInput.getText()) 
				|| JmsHelper.isStringNullOrEmpty(providerUrlInput.getText()))
			{
				JOptionPane.showMessageDialog(null, "One of Connection Factory, Provider URL or Context Factory Name is or empty", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		JmsConfig jmsConfig = new JmsConfig( 
											ctxFactoryClassInput.getText(),
											connFactNameInput.getText(),
											providerUrlInput.getText(),
											usernameInput.getText(),
											new String(passwordInput.getPassword())
											);
		return jmsConfig;
	}
	
	public JmsConfigTab () {
		Insets inset = new Insets(space, space, space, space);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = inset;
		JPanel configPanel = new JPanel(new GridBagLayout());
		
		JLabel ctxFactoryClassLabel = new JLabel("Initial Context Factory Class");
		ctxFactoryClassInput = new JTextField(30);
		ctxFactoryClassInput.setText("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		ctxFactoryClassInput.setEditable(false);
		ctxFactoryClassLabel.setHorizontalAlignment(JLabel.RIGHT);

		JLabel connFactNameLabel = new JLabel("Connection Factory Name");
		connFactNameInput = new JTextField();
		connFactNameInput.setEditable(false);
		connFactNameInput.setText("ConnectionFactory");
		connFactNameLabel.setHorizontalAlignment(JLabel.RIGHT);

		JLabel providerUrlLabel = new JLabel("Provider URL");
		providerUrlInput = new JTextField();
		providerUrlLabel.setHorizontalAlignment(JLabel.RIGHT);

		JLabel usernameLabel = new JLabel("Username");
		usernameInput = new JTextField();
		usernameLabel.setHorizontalAlignment(JLabel.RIGHT);

		JLabel passwordLabel = new JLabel("Password");
		passwordInput = new JPasswordField();
		passwordLabel.setHorizontalAlignment(JLabel.RIGHT);		
		
		JPanel testConfigPanel = new JPanel();
		testConfigButton = new JButton("Test Configuration");
		testConfigButton.addActionListener(new TestJmsConfiguration());
		testConfigPanel.add(testConfigButton);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		configPanel.add(ctxFactoryClassLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		configPanel.add(ctxFactoryClassInput, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(connFactNameLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(connFactNameInput, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(providerUrlLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(providerUrlInput, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(usernameLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(usernameInput, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(passwordLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(passwordInput, gbc);

		
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.gridy = 5;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(testConfigPanel, gbc);
		
		GridBagConstraints masterGbc = new GridBagConstraints();
		masterGbc.gridx = 0;
		masterGbc.gridy = 0;
		masterGbc.anchor = GridBagConstraints.NORTHWEST;
		
		this.add(configPanel, masterGbc);
		this.title = "JMS Config";
		
	}
	
	class TestJmsConfiguration implements ActionListener {
		private String username;
		private String connFactName;
		private String ctxFactoryClass;
		private String providerUrl;
		private String password;
		private JmsConfigTestWorker jmsConfigTest;
		
		public TestJmsConfiguration() {
			
		}

		public void actionPerformed(ActionEvent e) {
			connFactName = connFactNameInput.getText();
			ctxFactoryClass = ctxFactoryClassInput.getText();
			providerUrl = providerUrlInput.getText();
			username = usernameInput.getText();
			username = usernameInput.getText();
			char[] tpassword = passwordInput.getPassword();
			password = new String(tpassword);
			
			if(JmsHelper.isStringNullOrEmpty(connFactName) 
				|| JmsHelper.isStringNullOrEmpty(ctxFactoryClass) 
				|| JmsHelper.isStringNullOrEmpty(providerUrl))
			{
				JOptionPane.showMessageDialog(null, "One of Connection Factory, Provider URL or Context Factory Name is or empty", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			try {
				jmsConfigTest = new JmsConfigTestWorker(ctxFactoryClass, connFactName, providerUrl, username, password);
				Thread t = new Thread(jmsConfigTest);
				t.start();
			} catch (JmsDiggerException ex) {
				String s = "An error has occured while creating a JmsConfigTestWorker";
				LOG.info(s, ex);
				JmsGuiCommon.showErrorAndLogMessage(s);
			} 
			
		}
	}
	
	
	class JmsConfigTestWorker implements Runnable {
		private JmsInitialContextFactory contextFactory;
		private InitialContext ctx;
		private JmsLoginInfo loginInfo;
		private JmsAuthentication jmsAuthn;
		private boolean result;


		
		public JmsConfigTestWorker(String ctxFactoryClass, String connFactName, String providerUrl, String username, String password) throws JmsDiggerException {
			contextFactory = new JmsInitialContextFactory(ctxFactoryClass, providerUrl);
			contextFactory.addConnectionFactory(connFactName);
			ctx = contextFactory.getInitialContext();
			loginInfo = new JmsLoginInfo(username, password);
			jmsAuthn = new JmsAuthentication(ctx, connFactName);
			this.result = false;
			
		}

		public void run() {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					testConfigButton.setEnabled(false);
				}
			});
			
			try {
				result = jmsAuthn.isLoginInfoValid(loginInfo);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if(result == true)
							JOptionPane.showMessageDialog(null, "Configuration parameters OK!", "Success", JOptionPane.INFORMATION_MESSAGE);
						else
							JOptionPane.showMessageDialog(null, "Connection failed, please check logs for more details.", "Failure", JOptionPane.ERROR_MESSAGE);	
					}
				});

				
			} catch (JmsDiggerException e) {
				String errStr = "An error occured while checking login credentials";
				LOG.info(errStr, e);
				JmsGuiCommon.showErrorAndLogMessage(errStr);
			} finally {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						testConfigButton.setEnabled(true);
					}
				});
			}
			
		}

	}

}
