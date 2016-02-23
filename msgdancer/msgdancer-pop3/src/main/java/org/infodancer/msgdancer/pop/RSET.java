package org.infodancer.msgdancer.pop;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RSET extends AbstractPOPCommand
{
	static Pattern statPattern;
	
	static
	{
		statPattern = Pattern.compile("^RSET.*");
	}

	public boolean match(POPHandler handler, String line) 
	{
		if (statPattern.matcher(line).matches()) return true;
		else return false;
	}

	public void process(POPHandler handler, String line) 
	throws IOException, POPException 
	{
		Matcher command = statPattern.matcher(line);
		if (command.matches())
		{
			
			handler.writeLine("+OK Resetting user data");
		}
		else handler.writeLine("-ERR Unrecognized command."); 
	}
}
