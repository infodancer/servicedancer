package org.infodancer.msgdancer.smtp;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.infodancer.message.EmailAddress;

/** 
 * SMTP Extension supporting authentication.  
 **/

public class AUTH_LOGIN extends AbstractSMTPCommand implements SMTPCommandHandler, SMTPExtension
{
	private static final Logger log = Logger.getLogger(AUTH_LOGIN.class.getName());
	static Pattern plainPattern;
	
	static 
	{
		plainPattern = Pattern.compile("^AUTH LOGIN");
	}
	
	public boolean match(String line)
	{
		Matcher command = plainPattern.matcher(line);
		if (command.matches()) return true;
		else return false; 
	}
	
	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException
	{
		Matcher command = plainPattern.matcher(line);
		if (command.matches())
		{
			if (handler.authenticated != null)
			{
				handler.writeLine("503 Already authenticated as " + handler.authenticated);
				return;
			}

			command = plainPattern.matcher(line);
			if (command.matches())
			{
				handler.writeLine("334 " + Base64.encodeBase64String("Username".getBytes()));
				String username = new String(Base64.decodeBase64(handler.readLine()));
				handler.writeLine("334 " + Base64.encodeBase64String("Password".getBytes()));
				String password = new String(Base64.decodeBase64(handler.readLine()));
				processAuth(handler, username, password);
			}
		}
	}
	
	public String getExtensionName()
	{
		return "LOGIN";
	}
}
