package org.infodancer.msgdancer.pop;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.Message;

public class LIST extends AbstractPOPCommand implements POPCommand 
{	
	static Pattern listAllPattern;
	static Pattern listPattern;
	
	static
	{
		listPattern = Pattern.compile("^LIST.(.*)");
		listAllPattern = Pattern.compile("^LIST.*");
	}

	public boolean match(POPHandler handler, String line) 
	{
		if (listAllPattern.matcher(line).matches()) return true;
		else return false;
	}

	public void process(POPHandler handler, String line) 
	throws IOException, POPException 
	{
		if (handler.authenticated) 
		{
			Matcher command = listPattern.matcher(line);
			if (command.matches())
			{
				if (handler.messages != null)
				{
					int msgid = Integer.parseInt(command.group(1));
					Message msg = handler.messages.get(msgid);
					handler.writeLine("+OK " + msgid + " " + msg.size());
				}
				else handler.writeLine("-ERR Mailbox not available.");
			}
			else
			{
				command = listAllPattern.matcher(line);
				if (command.matches())
				{
					if (handler.messages != null)
					{
						handler.writeLine("+OK");
						int size = handler.messages.size();
						for (int i = 0; i < size; i++)
						{
							Message msg = handler.messages.get(i);
							if (msg != null)
							{
								handler.writeLine(i + " " + msg.size());
							}
						}
						handler.writeLine(".");
					}
					else handler.writeLine("-ERR Mailbox not available.");
				}
				else handler.writeLine("-ERR Unrecognized command.");
			}
		}
		else
		{
			handler.writeLine("-ERR Authenticate first.");
		}
	}
}
