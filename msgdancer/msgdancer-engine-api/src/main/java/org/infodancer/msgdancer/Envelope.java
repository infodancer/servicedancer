package org.infodancer.msgdancer;

import java.util.Set;

import org.infodancer.message.EmailAddress;

/**
 * The Envelope interface is designed to provide deliverability and anonymity.
 * @author matthew
 */

public interface Envelope
{
	/**
	 * Provides the sender address, which may be hashed.
	 * @return
	 */
	public String getSenderAddress();
	public void setSenderAddress(String sender);
	
	/**
	 * Provides the recipient address, which may be hashed.
	 * @return
	 */
	public String getRecipientAddress();
	public void setRecipientAddress(String recipient);
	
	/** 
	 * Provides the MX server to which the message should be delivered.
	 * By providing the mx in the envelope, we don't need to remember the domain. 
	 **/
	public String getMX();
	public void setMX(String mx);
	
	public String getMessageID();
	public void setMessageID(String id);
}
