package org.infodancer.msgdancer.smtp;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.EmailAddress;
import org.infodancer.message.InvalidAddressException;

public class RCPTTO extends AbstractSMTPCommand implements SMTPCommandHandler
{
	private static final Logger log = Logger.getLogger(RCPTTO.class.getName());	
	static Pattern rcptPattern;
	
	static 
	{
		rcptPattern = Pattern.compile("^(?i)RCPT.TO.<(.*)>");
	}

	public boolean match(String line)
	{
		Matcher command = rcptPattern.matcher(line);
		if (command.matches()) return true;
		else return false;
	}
	
	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException
	{
		Matcher command = rcptPattern.matcher(line);
		if (command.matches())
		{
			try
			{
				log.fine("RCPT TO:<" + command.group(1) + ">");
				EmailAddress recipient = new EmailAddress(command.group(1));
				handler.addRecipient(recipient);
				handler.writeLine("250 recipient <" + recipient + "> OK");
			}

			catch (SMTPException e)
			{
				handler.writeLine(e.getMessage());
			}	
			
			catch (InvalidAddressException e)
			{
				handler.writeLine("500 Unrecognized command or syntax error");
			}	
		}
	}
}
