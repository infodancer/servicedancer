package org.infodancer.msgdancer.smtp;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.EmailAddress;
import org.infodancer.message.InvalidAddressException;

public class MAILFROM extends AbstractSMTPCommand implements SMTPCommandHandler
{
	private static final Logger log = Logger.getLogger(MAILFROM.class.getName());
	static Pattern mailPattern;
	static Pattern bouncePattern;
	
	static
	{
		mailPattern = Pattern.compile("^MAIL.FROM.<(.*)>");
		bouncePattern = Pattern.compile("^MAIL.FROM.<>");
	}
	
	public boolean match(String line)
	{
		if (bouncePattern.matcher(line).matches()) return true;
		if (mailPattern.matcher(line).matches()) return true;
		return false;
	}
	
	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException, SMTPException
	{
		Matcher command = bouncePattern.matcher(line);
		if (command.matches())
		{
			if (handler.remoteHelo == null) 
			{
				handler.writeLine("503 Send HELO first.");
			}
			else
			{
				try
				{
					log.fine("MAIL FROM:<>");
					handler.setBounceMessage(true);
					handler.writeLine("250 OK");
				}

				catch (Exception e)
				{
					handler.writeLine("451 I can't queue your message right now.  Please try again later.");
				}
			}
		}

		command = mailPattern.matcher(line);
		if (command.matches())
		{
			if (handler.remoteHelo == null) 
			{
				handler.writeLine("503 Send HELO first.");
			}
			else
			{
				try
				{
					log.fine("MAIL FROM:<" + command.group(1) + ">");
					EmailAddress sender = new EmailAddress(command.group(1));
					handler.setSender(sender);
					handler.writeLine("250 sender <" + sender + "> OK");
				}
				
				catch (InvalidAddressException e)
				{
					handler.writeLine("550 " + e.getMessage());
				}

				catch (Exception e)
				{
					handler.writeLine("451 I can't queue your message right now.  Please try again later.");
				}
			}
		}
	}
}
