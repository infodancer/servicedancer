package org.infodancer.msgdancer.smtp;

import java.util.regex.*;

public class NOOP extends AbstractSMTPCommand implements SMTPCommandHandler
{
	static Pattern noopPattern;
	
	static 
	{
		noopPattern = Pattern.compile("^(?i)NOOP.*");
	}
	
	public boolean match(String line)
	{
		Matcher command = noopPattern.matcher(line);
		if (command.matches()) return true;
		else return false;
	}
	
	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException
	{
		handler.writeLine("250 OK");
	}
}
