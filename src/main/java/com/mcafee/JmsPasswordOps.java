package com.mcafee;

import java.util.ArrayList;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

/**
 * 
 * @author Gursev Singh Kalra @ McAfee, Inc.
 *
 */
public class JmsPasswordOps {
	private ArrayList<String> passwords = new ArrayList<String>();
	private StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();    

	public void addPassword(String password) {
		if(password == null)
			throw new IllegalArgumentException("Password cannot be null");
		this.passwords.add(password);
	}
	
	public void addPasswordList(ArrayList<String> passwords) {
		if(passwords == null || passwords.size() == 0) 
			throw new IllegalArgumentException("Password ArrayList cannot be null or of zero length");
		
		//Perform a deep copy
		for(String pass: passwords) {
			if(pass != null)
				this.passwords.add(pass);
		}
	}
	
	public void clearPasswords() {
		this.passwords.clear();
	}
	
	
	public String decryptOne(String encPassword, String key) {
		String result = null;
		if(JmsHelper.isStringNullOrEmpty(encPassword) || key == null || key.equals("")) {
			throw new IllegalArgumentException("EncryptedText or password cannot be null or empty");
		}
		
		decryptor = new StandardPBEStringEncryptor();
		
		try {
			decryptor.setPassword(key);
			result = decryptor.decrypt(encPassword);
		} catch (EncryptionOperationNotPossibleException ex) {
			//Absorb this exception 
		}
		return result;
	}
	
	public String decrypt(String encryptedText) {
		String result = null;
		if(encryptedText == null) 
			throw new IllegalArgumentException("Encrypted text cannot be null");

		if(passwords.size() == 0) 
			throw new IllegalArgumentException("No password list provided");
		
		for(String pass : passwords) {
			//New object is required for each decryption attempt
			decryptor = new StandardPBEStringEncryptor();
			try {
				decryptor.setPassword(pass);
				result = decryptor.decrypt(encryptedText);
			} catch (EncryptionOperationNotPossibleException ex) {
				//Absorb this exception to be able to run through a large number of passwords 
			}
		}			
		// A null value for the result indicates that encrypted 
		// text could not be decrypted with the provided passwords
		return result;
	}
}
