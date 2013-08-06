package com.mcafee;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Random;

/**
 * 
 * @author Gursev Singh Kalra  @ McAfee, Inc.
 *
 */
class Person {
	private String name;
	private int age;
	
	
	public Person(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}
	
	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}	
	
}

/**
 * This class generates content for JMS messages.
 * @author Gursev Singh Kalra
 *
 */
public class JmsMessageContentGenerator {
	
	private static final char[] printable = { 
		'a','b','c','d','e','f','g','h','i','j',
		'k','l','m','n','o','p','q','r','s','t',
		'u','v','w','x','y','z','A','B','C','D',
		'E','F','G','H','I','J','K','L','M','N',
		'O','P','Q','R','S','T','U','V','W','X',
		'Y','Z','0','1','2','3','4','5','6','7',
		'8','9','`','!','@','#','$','%','^','&',
		'*','(',')','-','=','_','+','[',']','\\',
		'|','}','{',';','\'',':','"','/','.','\'',
		'<','>','?','~'
	};
	
	private static final char[] alnum = { 
		'a','b','c','d','e','f','g','h','i','j',
		'k','l','m','n','o','p','q','r','s','t',
		'u','v','w','x','y','z','A','B','C','D',
		'E','F','G','H','I','J','K','L','M','N',
		'O','P','Q','R','S','T','U','V','W','X',
		'Y','Z','0','1','2','3','4','5','6','7',
		'8','9'
	};
	
	private static final Random random = new Random();
	private static final int LEN = 50;
	private int max_value = 256;
	
	/**
	 * Generate and return alphanumeric string
	 * @return
	 */
	public String getAlnumString() {
		return getPrintableString(random.nextInt(LEN)); // do not want to return zero length strings
	}
	
	public void setMaxValue(int i) {
		max_value = i;
	}
	
	/**
	 * Get alphanumeric string with a particular length
	 * @param length
	 * @return
	 */
	public String getAlnumString(int length) {
		int plen = alnum.length;
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < length; i++) {
			sb.append(alnum[random.nextInt(plen)]);
		}
		return sb.toString();	
	}
	
	
	/**
	 * generates String with length upto 50 characters. 
	 * For longer Strings, use getPrintableString(int length) method
	 * @return
	 */
	public String getPrintableString() {
		return getPrintableString(random.nextInt(LEN)); // do not want to return zero length strings
	}
	
	/**
	 * Create a printable string
	 * @param length
	 * @return
	 */
	public String getPrintableString(int length) {
		int plen = printable.length;
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < length; i++) {
			sb.append(printable[random.nextInt(plen)]);
		}
		return sb.toString();	
	}
	
	
	public String getString() {
		return getString(random.nextInt(LEN) + 1);
		
	}
	
	/**
	 * Creates a string with all characters less than max_value
	 * @param length
	 * @return
	 */
	public String getString(int length) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < length; i++) {
			sb.append((char)(random.nextInt(max_value)));
		}
		return sb.toString();
	}	
	
	
	/**
	 * Create a byte array with random length
	 * @return
	 */
	public byte[] getByteAray() {
		return getByteAray(random.nextInt(LEN) + 1);
	}
	
	/**
	 * Create a byte array with length 
	 * @param length - length of the byte array
	 * @return
	 */
	public byte[] getByteAray(int length) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(int i = 0 ; i < length; i++) {
			baos.write(random.nextInt(256));
		}
		return baos.toByteArray();
	}

	/**
	 * Creates a Person object and returns
	 * @return
	 */
	public Object getObject() {
		return new Person(getPrintableString(), random.nextInt(LEN));
		
	}
	
	/**
	 * creates a HashTable object of random length
	 * @return HashTable
	 */
	public Hashtable<String, String> getMap() {
		return getMap(random.nextInt(LEN));
	}
	
	/**
	 * creates a HashTable object of specified length
	 * @param size - size of the HashTable
	 * @return HashTable
	 */
	public Hashtable<String, String> getMap(int size) {
		Hashtable<String, String> map = new Hashtable<String, String>();
		for(int i = 0 ; i < size; i ++)
			map.put(getPrintableString(), getString());
		return map;
	}
	

}
