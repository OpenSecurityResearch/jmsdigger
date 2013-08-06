package com.mcafee.gui;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class ProgressInfo {
	private String description;
	private boolean successful;
	private int percentage;
	
	public ProgressInfo() {
		
	}
	
	public ProgressInfo(String description, boolean successful, int percentage) {
		this.description = description;
		this.successful = successful;
		this.percentage = percentage ;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	public String getDescription() {
		if(description == null)
			return "";
		return description;
	}
	
	public boolean isSuccessful() {
		return successful;
	}
	
	public int getPercentage() {
		return percentage;
	}
}
