package org.infodancer.msgdancer.smtp;

import java.util.regex.*;

public class RSET extends AbstractSMTPCommand  implements SMTPCommandHandler
{
	static Pattern rsetPattern;
	
	static 
	{
		rsetPattern = Pattern.compile("^(?i)RSET.*");
	}
	
	public boolean match(String line)
	{
		Matcher command = rsetPattern.matcher(line);
		if (command.matches()) return true;
		else return false;		
	}
	
	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException
	{
		handler.clear();
		handler.writeLine("250 OK Server state reset");
	}
}
