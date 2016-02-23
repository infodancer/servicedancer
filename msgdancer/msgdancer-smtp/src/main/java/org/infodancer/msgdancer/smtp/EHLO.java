package org.infodancer.msgdancer.smtp;

import java.util.regex.*;

import java.util.logging.Logger;

public class EHLO extends AbstractSMTPCommand implements SMTPCommandHandler
{
	private static final Logger log = Logger.getLogger(EHLO.class.getName());
	static Pattern ehloPattern;
	
	static 
	{
		ehloPattern = Pattern.compile("^(?i)EHLO.(.*)");
	}
	
	public boolean match(String line)
	{
		Matcher command = ehloPattern.matcher(line);
		if (command.matches()) return true;
		else return false;
	}
	
	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException
	{
		Matcher command = ehloPattern.matcher(line);
		if (command.matches())
		{
			log.fine("Remote server EHLO: " + command.group(1));
			handler.remoteHelo = command.group(1);
			// msgengine.verifyRemoteHelo(remoteHelo, remoteHostname, remoteAddress);
			handler.writeLine("250-" + handler.getServerName());
			handler.writeLine("250-AUTH LOGIN PLAIN");
			handler.writeLine("250-PIPELINING");
			handler.writeLine("250 SIZE " + handler.service.getSize());
		}
	}
}
