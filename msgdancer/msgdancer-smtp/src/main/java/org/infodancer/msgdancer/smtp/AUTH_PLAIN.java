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

public class AUTH_PLAIN extends AbstractSMTPCommand implements SMTPCommandHandler, SMTPExtension
{
	private static final Logger log = Logger.getLogger(AUTH_PLAIN.class.getName());
	static Pattern plainPattern;
	static Pattern shortPattern;
	
	static 
	{
		plainPattern = Pattern.compile("^AUTH PLAIN");
		shortPattern = Pattern.compile("^AUTH PLAIN (.*)");
	}
	
	public boolean match(String line)
	{
		Matcher command = plainPattern.matcher(line);
		if (command.matches()) return true;
		else 
		{
			command = shortPattern.matcher(line);
			return command.matches();
		}
	}
	
	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException
	{
		Matcher command = shortPattern.matcher(line);
		if (command.matches())
		{
			if (handler.authenticated != null)
			{
				handler.writeLine("503 Already authenticated as " + handler.authenticated);
				return;
			}
			
			String authline = command.group(1);
			if ((authline != null) && (authline.trim().length() > 0))
			{
				processAuthline(handler, authline);
			}
		}
		else
		{
			command = plainPattern.matcher(line);
			if (command.matches())
			{
				handler.writeLine("334 " + Base64.encodeBase64String("Go ahead; make my day.".getBytes()));
				processAuthline(handler, handler.readLine());
			}
		}
	}
	
	public void processAuthline(SMTPProtocol handler, String authline) 
	throws IOException
	{
		if ((authline != null) && (authline.trim().length() > 0))
		{
			String decoded = new String(Base64.decodeBase64(authline));
			String[] authsplit = decoded.split(" ");
			if (authsplit.length == 2)
			{				
				String username = authsplit[0];
				String password = authsplit[1];
				if ((password != null) && (username != null))
				{
					processAuth(handler, username, password);
				}
				else handler.writeLine("500 Syntax error; no token matched! (short)");
			}
			else handler.writeLine("500 Syntax error");
		}
	}
		
	public String getExtensionName()
	{
		return "PLAIN";
	}
}
