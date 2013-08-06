package com.mcafee.gui;

import javax.naming.InitialContext;

import com.mcafee.JmsDiggerException;
import com.mcafee.JmsInitialContextFactory;
import com.mcafee.JmsLoginInfo;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsConfig {
	private InitialContext initialContext;
	private JmsInitialContextFactory contextFactory;
	private String ctxFactoryClass;
	private String connFactName;
	private String providerUrl;
	private String username;
	private String password;
	private JmsLoginInfo loginInfo;
	private boolean finalInitContext;
	
	public JmsConfig(String ctxFactoryClass, String connFactName, String providerUrl, String username, String password) throws JmsDiggerException {
		this.ctxFactoryClass = ctxFactoryClass;
		this.connFactName = connFactName;
		this.providerUrl = providerUrl;
		this.username = username;
		this.password = password;
		contextFactory = new JmsInitialContextFactory(this.getCtxFactoryClass(), this.getProviderUrl());
		contextFactory.addConnectionFactory(this.getConnFactName());
		loginInfo = new JmsLoginInfo(this.username, this.password);
		finalInitContext = false;
	}
	
	public InitialContext buildInitialContext() throws JmsDiggerException {
		//Delaying Initial Context initialization until it is retrieved.
		if(!finalInitContext) 
			initialContext = contextFactory.getInitialContext();
		return initialContext;
	}
	
	public void addTopic(String topicName) {
		if(!finalInitContext)
			contextFactory.addTopic(topicName, "jms."+topicName);
	}
	
	public void addQueue(String queueName) {
		if(!finalInitContext)
			contextFactory.addQueue(queueName, "jms."+queueName);
	}

	public String getCtxFactoryClass() {
		return ctxFactoryClass;
	}

	public String getConnFactName() {
		return connFactName;
	}

	public String getProviderUrl() {
		return providerUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public JmsLoginInfo getLoginInfo() {
		return loginInfo;
	}

}
