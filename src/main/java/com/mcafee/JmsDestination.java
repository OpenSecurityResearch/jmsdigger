package com.mcafee;

/**
 * Defines the type of JMS destination
 * @author Gursev Singh Kalra  @ McAfee, Inc.
 *
 */
public enum JmsDestination {
	QUEUE,
	TOPIC,
	CONNECTIONFACTORY,
	DURABLESUBSCRIBER,
	BROKER,
	SUBSCRIPTION
}
