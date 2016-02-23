package org.infodancer.msgdancer.smtp;

import java.util.regex.*;

public class QUIT extends AbstractSMTPCommand  implements SMTPCommandHandler
{
	static Pattern quitPattern;
	
	static 
	{
		quitPattern = Pattern.compile("^(?i)QUIT.*");
	}
	
	public boolean match(String line)
	{
		Matcher command = quitPattern.matcher(line);
		if (command.matches()) return true; 
		else return false;
	}

	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException, SMTPException
	{
		handler.stop();
	}
}
