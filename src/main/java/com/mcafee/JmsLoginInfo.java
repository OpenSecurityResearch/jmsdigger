package com.mcafee;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsLoginInfo {
	private String username;
	private String password;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public JmsLoginInfo(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	
	public String toString()
	{
		String uname;
		String passwd;
		
		uname = (username == null)? "null":username;
		passwd = (password == null)? "null":password;
		return "Username: \"" +uname+"\", Password: \"" +passwd + "\"";
	}

}
