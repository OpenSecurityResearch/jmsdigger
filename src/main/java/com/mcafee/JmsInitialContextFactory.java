package com.mcafee;
import java.util.Properties;

//import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class enables generation of InitailContexts. It does not use username or password from  
 * the environment but relies on programmatically providing these values.
 * If someone wants to add username and password the following properties can be added 
 * via addRawPropertyToEnv method.
 * 
 * java.naming.security.principal -- for username
 * java.naming.security.credentials -- for password
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */

public class JmsInitialContextFactory {
	private static final Logger LOG = LoggerFactory.getLogger(JmsInitialContextFactory.class);
	
	private boolean editable;
	private Properties env = new Properties();
	private String connectionFactoryName;
	
	public JmsInitialContextFactory() {
		editable = true;
	}

	/**
	 * This constructor initializes the environment with InitialContextFactory name and the provider URL.
	 * These are mandatory parameters as there is no point initializing if these values are not available
	 * 
	 * @param initialContextFactory
	 * @param providerUrl
	 */
	public JmsInitialContextFactory(String initialContextFactory, String providerUrl)
	{
		env.setProperty("java.naming.factory.initial", initialContextFactory);
		env.setProperty("java.naming.provider.url", providerUrl);
		editable = true;
	}
	
	public String getConnectionFactoryName() {
		return connectionFactoryName;
	}
	
	/**
	 * This method takes two parameters to add a new Queue to the Context Environment. 
	 * The topicName parameter takes the name of the Queue, prepends it with "queue." before populating it in the environment.
	 * This is required because that is how the destination name resolution happens.  Queues must be prepended by "queue.".
	 * The displayName is the name that gets displayed when connection is established.
	 * @param queueName
	 * @param displayName
	 */
	public void addQueue(String queueName, String displayName) {
		if(this.editable == false)
			return;
		env.setProperty("queue." + queueName, displayName);
	}
	

	/**
	 * This method allows more control over what gets added. 
	 * One can choose to not use "topic." or "queue." prependers when adding to the environment variable env
	 * @param destination
	 * @param displayName
	 */
	public void addRawPropertyToEnv(String property, String displayName) {
		if(this.editable == false)
			return;
		env.setProperty(property, displayName);
	}

	/**
	 * This method takes two parameters to add a new Topic to the Context Environment. 
	 * The topicName parameter takes the name of the Topic, prepends it with "topic." before populating it in the environment.
	 * This is required because that is how the resolution happens. Queues must be prepended by "queue.".
	 * The displayName is the name that gets displayed when connection is established.
	 * @param topicName
	 * @param displayName
	 */
	
	public void addTopic(String topicName, String displayName) {
		if(this.editable == false)
			return;
		env.setProperty("topic." + topicName, displayName);
	}	
	
	/**
	 * This method adds connectionFactoryNames to the initial context.
	 * For connection factory lookup to work, the name must be registered here. If no registration is performed,
	 * "ConnectionFactory" is the default name that can be used to access the ConnectionFactory from the JNDI service
	 * @param connectionFactoryName
	 */
	public void addConnectionFactory(String connectionFactoryName)
	{
		if(this.editable == false)
			return;
		this.connectionFactoryName = connectionFactoryName;
		env.setProperty("connectionFactoryNames", connectionFactoryName);
	}


	/**
	 * This method generates an Initial context, marks the InitialContextFactory object as non-editable before returning the 
	 * InitialContext
	 * @param result
	 * @return
	 * @throws JmsDiggerException 
	 */
	public InitialContext getInitialContext() throws JmsDiggerException {
		LOG.debug("Entering getInitialContext");
		
		InitialContext ctx = null;
		
		// Default "connectionFactoryNames" to "ConnectionFactory" if not initialized
		// "ConnectionFactory" is overwritten when the value of "connectionFactoryNames" is set previously. So if a program 
		// tries to access "ConnectionFactory" after "connectionFactoryNames" is set to something, javax.naming.NameNotFoundException
		// is thrown
		if(env.getProperty("connectionFactoryNames") == null)
		{
			this.connectionFactoryName = "ConnectionFactory";
			env.setProperty("connectionFactoryNames", "ConnectionFactory");
		}
		
		try
		{
			ctx = new InitialContext(this.env);
		}
		catch (NamingException ne)
		{
			LOG.info("Initial Context could not be created", ne);
			throw JmsHelper.buildJmsDiggerException("Initial Context could not be created", ne);
		}
		
		this.editable = false;
		LOG.debug("Leaving getInitialContext");
		return ctx;
	}

}
