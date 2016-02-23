package org.infodancer.msgdancer;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.infodancer.message.DeliveryException;
import org.infodancer.message.EmailAddress;
import org.infodancer.service.api.ServiceEngine;

/** 
 * The MessageEngine handles message notifications and deliveries, not message storage.
 * Messages being sent are stored in their users outbound folders.  
 **/
 
public interface MessageEngine extends ServiceEngine 
{
	public static final String CONTEXT_MESSAGE_ENGINE = "msgdancer";
	public static final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";
	
	public boolean isLocalAddress(EmailAddress address);
	public boolean verifyEmailAddress(EmailAddress address);
	
	/** 
	 * Hashes an email address for use in a MessageEnvelope.
	 */
	public String hash(EmailAddress address);
	
	/**
	 * Stores a message within the MessageEngine, and returns a message id as a unique identifier.
	 */
	public String store(byte[] data) throws MessageEngineException;
	
	/**
	 * Retrieves a message from the MessageEngine.
	 */
	public byte[] retrieve(String msgid) throws MessageEngineException;
	
	/**
	 * Queues the provided msgid (which must have already been created with store()) for delivery.
	 * This method can be used for local or remote deliveries.
	 */
	public void enqueue(String msgid, EmailAddress recipient) throws MessageEngineException;
	
	/**
	 * Determines whether or not a given msgid exists without actually reading it.
	 */
	public boolean exists(String msgid) throws MessageEngineException;
	
	/**
	 * Retrieves a list of UUIDs bound for a specific recipient.
	 */
	public abstract List<String> getMessages(EmailAddress recipient) throws MessageEngineException;
}