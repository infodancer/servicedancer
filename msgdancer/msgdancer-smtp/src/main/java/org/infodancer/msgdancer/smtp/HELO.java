package org.infodancer.msgdancer.smtp;

import java.util.regex.*;

import java.util.logging.Logger;

public class HELO extends AbstractSMTPCommand implements SMTPCommandHandler
{
	private static final Logger log = Logger.getLogger(HELO.class.getName());
	static Pattern heloPattern;
	
	static 
	{
		heloPattern = Pattern.compile("^(?i)HELO.(.*)");
	}
	
	public boolean match(String line)
	{
		Matcher command = heloPattern.matcher(line);
		if (command.matches()) return true;
		else return false;
 
	}

	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException
	{
		Matcher command = heloPattern.matcher(line);
		if (command.matches()) 
		{
			handler.writeLine("250 " + handler.getServerName());
			if (line.length() > 5)
			{
				handler.remoteHelo = line.substring(5, line.length());
			}
		} 
	}
}
