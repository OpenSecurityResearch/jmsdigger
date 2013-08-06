package com.mcafee.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcafee.JmsDiggerException;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
class JmsDiggerFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 700;
	private static final int HEIGHT = 690;
	private static final Logger LOG = LoggerFactory.getLogger(JmsDiggerFrame.class);
	
	public JmsDiggerFrame() {
		
	}
	
	public JmsDiggerFrame(String name) {
		super(name);
	}
	
	public String getString() {
		return "string";
	}
	
	private static void createAndShowGUI() throws JmsDiggerException {		
		JmsDiggerFrame jmsDiggerFrame = new JmsDiggerFrame("JMSDigger 0.1");
		JPanel jmsDiggerParentPanel = new JPanel(new GridLayout(1, 1));
		JTabbedPane tabbedPane = new JTabbedPane();
		JmsConfigTab jmsConfigTab = new JmsConfigTab();
		JmsAuthenticationTab authenticationTab = new JmsAuthenticationTab(jmsConfigTab);
		JmsDumpDestinationTab dumpTab = new JmsDumpDestinationTab(jmsConfigTab);
		JmsDurableSubscriberTab durableSubscriberTab = new JmsDurableSubscriberTab(jmsConfigTab);
		AmqOpsTab amqOpsTab = new AmqOpsTab(jmsConfigTab);
		JMSDiggerAboutTab aboutTab = new JMSDiggerAboutTab();
		
		jmsDiggerFrame.setSize(WIDTH, HEIGHT);
		jmsDiggerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// All the tabs will be added here
		tabbedPane.add(jmsConfigTab.getTitle(), jmsConfigTab);
		tabbedPane.add(authenticationTab.getTitle(), authenticationTab);
		tabbedPane.add(amqOpsTab.getTitle(), amqOpsTab);
		tabbedPane.add(dumpTab.getTitle(), dumpTab);
		tabbedPane.add(durableSubscriberTab.getTitle(), durableSubscriberTab);
		tabbedPane.add(aboutTab.getTitle(), aboutTab);
		
		jmsDiggerParentPanel.add(tabbedPane);
		jmsDiggerFrame.add(jmsDiggerParentPanel, BorderLayout.NORTH);
		jmsDiggerFrame.setVisible(true);				
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					createAndShowGUI();
				} catch (JmsDiggerException e) {
					String errStr = "An error occured while creating JMSDigger GUI";
					LOG.info(errStr, e);
					JmsGuiCommon.showErrorAndLogMessage(errStr);

				}
            }
        });
	}
}
