package org.infodancer.msgdancer.smtp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.EmailAddress;

public class VRFY extends AbstractSMTPCommand implements SMTPCommandHandler
{
	static Pattern vrfyPattern;
	
	static 
	{
		vrfyPattern = Pattern.compile("^(?i)VRFY (.*)");
	}
	
	public boolean match(String line)
	{
		Matcher command = vrfyPattern.matcher(line);
		if (command.matches()) return true;
		else return false;		
	}
	
	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException
	{
		Matcher command = vrfyPattern.matcher(line);
		if (command.matches())
		{
			String username = command.group(1);
			if (username != null)
			{
				try
				{
					EmailAddress recipient = new EmailAddress(username);
					if (msgengine.verifyEmailAddress(recipient))
					{
						handler.writeLine("250 <" + username + ">");
					}
					else handler.writeLine("553 User unknown");
				}
				
				catch (Exception e)
				{
					handler.writeLine("553 User ambiguous");
				}
			}
			else handler.writeLine("553 User ambiguous");
		}
	}
}
