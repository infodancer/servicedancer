package org.infodancer.msgdancer.pop;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.Message;

public class STAT extends AbstractPOPCommand implements POPCommand 
{
	static Pattern statPattern;
	
	static
	{
		statPattern = Pattern.compile("^STAT.*");
	}

	public boolean match(POPHandler handler, String line) 
	{
		if (statPattern.matcher(line).matches()) return true;
		else return false;
	}

	public void process(POPHandler handler, String line) 
	throws IOException, POPException 
	{
		if (handler.authenticated) 
		{
			Matcher command = statPattern.matcher(line);
			if (command.matches())
			{
				if (handler.messages != null)
				{
					int size = 0;
					long totalSize = 0;
					for (Message message : handler.messages)
					{
						size++;
						totalSize += message.size();
					}
					handler.writeLine("+OK " + size + " " + totalSize);					
				}
				else handler.writeLine("-ERR Mailbox not available.");
			}
			else handler.writeLine("-ERR Unrecognized command."); 
		}
		else handler.writeLine("-ERR Authenticate first.");
	}
}
