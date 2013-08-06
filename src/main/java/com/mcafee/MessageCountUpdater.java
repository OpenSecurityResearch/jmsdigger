package com.mcafee;
import java.awt.EventQueue;
import javax.swing.JLabel;

/**
 * 
 * @author Gursev Singh Kalra  @ McAfee, Inc.
 *
 */
public class MessageCountUpdater implements Runnable {
	private JLabel progressLabel;
	private int count;
	


	public MessageCountUpdater(JLabel progressLabel) {
		this.progressLabel = progressLabel;
	}
	
	public void setCount(int count) {
		this.count = count;
	}

	public void run() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				progressLabel.setText(Integer.toString(count));					
			}
		});
	}
}
