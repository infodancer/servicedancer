package org.infodancer.msgdancer.smtp;

import java.util.concurrent.Callable;

import org.infodancer.message.EmailAddress;
import org.infodancer.msgdancer.Envelope;
import org.infodancer.msgdancer.MessageEngine;

public class SMTPDeliveryAction implements Callable<String>
{
	byte[] msgdata;
	EmailAddress recipient;
	EmailAddress sender;
	MessageEngine msgengine;
	SMTPService service;
	
	public SMTPDeliveryAction(SMTPService service, MessageEngine msgengine, EmailAddress sender, EmailAddress recipient, byte[] msgdata)
	{
		this.service = service;
		this.msgdata = msgdata;
		this.msgengine = msgengine;
		this.sender = sender;
		this.recipient = recipient;
	}
	
	@Override
	public String call() throws Exception
	{
		msgdata = service.processSpamCheck(recipient, msgdata);
	
		Object key = service.findPublicKey(recipient); 
		if (key != null)
		{
			msgdata = service.crypt(key, msgdata);
		}
		
		String msgid = msgengine.store(msgdata);
		msgengine.enqueue(msgid, recipient);
		return msgid;
	}
}
