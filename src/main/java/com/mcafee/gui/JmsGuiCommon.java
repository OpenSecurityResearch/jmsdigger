package com.mcafee.gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;

import javax.swing.JOptionPane;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsGuiCommon {
	public static void resetGridBagConstraints(GridBagConstraints gbc) {
		// Defaults from http://docs.oracle.com/javase/tutorial/uiswing/layout/gridbag.html
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.insets.left = 0;
		gbc.insets.right = 0;
		gbc.insets.bottom = 0;
		gbc.insets.top = 0;
	}
	
	public static void showErrorAndLogMessage() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, "An error has occured, please check logs for more details.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	public static void showErrorAndLogMessage(final String str) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, str+"\nPlease see log files for more details", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
	}
	
	public static void showDoneMessage() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, "Done!", "", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
}
