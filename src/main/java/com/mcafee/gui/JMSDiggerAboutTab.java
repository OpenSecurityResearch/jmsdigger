package com.mcafee.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class JMSDiggerAboutTab extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	
	public String getTitle() {
		return this.title;
	}
	public JMSDiggerAboutTab() {
		this.title = "About";
		JPanel aboutPanel = new JPanel(new GridBagLayout());
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		aboutPanel.setBorder(border);
		JTextArea aboutTextArea = new JTextArea("\n JMSDigger is an Enterprise Messaging Application Assessment tool. \n\n Author: Gursev Singh Kalra (gursev.kalra@foundstone.com)\n McAfee, Inc - Foundstone Professional Services. \n\n Source code: \n https://github.com/OpenSecurityResearch/jmsdigger\n ");
		aboutTextArea.setEditable(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		aboutPanel.add(aboutTextArea, gbc);
		this.add(aboutPanel);
	}
	
	

}
