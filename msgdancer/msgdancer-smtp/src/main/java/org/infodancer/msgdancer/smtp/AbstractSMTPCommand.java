package org.infodancer.msgdancer.smtp;

import java.io.IOException;
import java.util.logging.Logger;

import org.infodancer.message.EmailAddress;
import org.infodancer.msgdancer.MessageEngine;

public abstract class AbstractSMTPCommand implements SMTPCommandHandler
{
	private static final Logger log = Logger.getLogger(AbstractSMTPCommand.class.getName());
	MessageEngine msgengine;
		
	public void setMessageEngine(MessageEngine msgengine)
	{
		this.msgengine = msgengine;
	}
	
	public void processAuth(SMTPProtocol handler, String username, String password) throws IOException
	{
		try
		{
			EmailAddress authemail = new EmailAddress(username);
			handler.authdomain = handler.dm.getDomain(authemail.getDomain());
			if (handler.authdomain != null)
			{
				handler.um = handler.authdomain.getUserManager();
				handler.authuser = handler.um.authenticate(authemail.getUser(), password);
				
				if (handler.authuser != null)
				{
					log.info("User authenticated as " + authemail);
					handler.writeLine("235 2.7.0 Authentication successful.");
					handler.authenticated = authemail;
					handler.envelope = null;
				}
				else 
				{
					handler.writeLine("535 Authentication failed.");
					log.info("User failed to authenticate as " + authemail);
				}
			}
			else 
			{
				handler.writeLine("535 Authentication failed.");
				log.info("User tried to authenticate as unknown domain " + authemail);
			}
		}
			
		catch (Exception e)
		{
			e.printStackTrace();
			handler.writeLine("451 " + e.getMessage());
		}
	}
}
