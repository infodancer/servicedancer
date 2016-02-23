package org.infodancer.msgdancer.pop;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.EmailAddress;

public class USER extends AbstractPOPCommand implements POPCommand 
{
	static Pattern userPattern;
	
	static 
	{
		userPattern = Pattern.compile("^USER.(.*)");
	}
	
	public boolean match(POPHandler handler, String line) 
	{
		if (userPattern.matcher(line).matches()) return true;
		else return false;
	}

	public void process(POPHandler handler, String line) 
	throws IOException, POPException 
	{
		try
		{
			Matcher command = userPattern.matcher(line);
			if (command.matches())
			{
				handler.authenticated = false;
				handler.user = new EmailAddress(command.group(1));
				handler.writeLine("+OK Please go on.");
			}
		}
		
		catch (Exception e)
		{
			POPException ee = new POPException(e);
			ee.printStackTrace();
			throw ee;
		}
	}
}
