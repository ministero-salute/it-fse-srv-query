package it.finanze.sanita.fse2.ms.srvquery.utility;

import org.jasypt.util.text.AES256TextEncryptor;


/** 
 * Utility Class used to encrypt and decrypt strings 
 * 
 */
public class EncryptDecryptUtility {

	private EncryptDecryptUtility() {
		
	} 
	
	
    /** 
     *  Method for the encrypt.
     *  @param pwd  The pwd for encryption 
     *  @param msg  The message to encrypt 
     *  @return String  The encrypted string 
     */
	public static final String encrypt(String pwd, String msg) {
	    AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
    	 
    	textEncryptor.setPassword(pwd);
    	return textEncryptor.encrypt(msg);
	}

	/** 
     *  Method for decrypt. 
     *  
     *  @param pwd  The pwd for decryption 
     *  @param cryptedMsg  The crypted message 
     *  @return String  The decrypted string 
     */
	public static final String decrypt(String pwd, String cryptedMsg) {
    	AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
    	textEncryptor.setPassword(pwd);
    	return textEncryptor.decrypt(cryptedMsg);
	}


     /** 
     *  Encrypt method for generic object. 
     *  
     *  @param pwd  The pwd for the encryption 
     *  @param obj  The object to encrypt
     *  @return String  The converted string 
     */
	public static final String encryptObject(String pwd, Object obj) {
		String json = StringUtility.toJSON(obj);
		return encrypt(pwd, json);
	}

	public static final <T> T decryptObject(String pwd, String cryptedMsg, Class<T> cls) {
		String json = decrypt(pwd, cryptedMsg);
		return StringUtility.fromJSON(json, cls);
	}
    
}
