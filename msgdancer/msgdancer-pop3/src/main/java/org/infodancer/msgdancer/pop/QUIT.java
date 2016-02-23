package org.infodancer.msgdancer.pop;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.Message;

public class QUIT extends AbstractPOPCommand implements POPCommand 
{
	static Pattern quitPattern;
	
	static 
	{
		quitPattern = Pattern.compile("^(?i)QUIT.*");
	}
	
	public boolean match(POPHandler handler, String line)
	{
		Matcher command = quitPattern.matcher(line);
		if (command.matches()) return true; 
		else return false;
	}

	public void process(POPHandler handler, String line)
	throws java.io.IOException, POPException
	{
		try
		{
			if (handler.authenticated)
			{
				if (handler.messages != null)
				{
					for (Message message : handler.messages)
					{
						if (message != null) 
						{
							if (message.isFlag(Message.Flags.DELETED))
							{
								message.delete();
							}
						}
					}
					handler.folder.close();
					handler.folder = null;
					handler.manager = null;
					handler.messages = null;
				}
			}
			handler.writeLine("+OK Thanks for visiting");
			handler.stop();
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			handler.writeLine("-ERR Some deleted messages not removed.");
		}
	}
}
