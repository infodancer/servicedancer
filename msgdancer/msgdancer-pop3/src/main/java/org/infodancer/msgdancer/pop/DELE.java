package org.infodancer.msgdancer.pop;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.Message;

public class DELE extends AbstractPOPCommand implements POPCommand
{	
	static Pattern delePattern;

	static
	{
		delePattern = Pattern.compile("^DELE.(.*)");
	}

	public boolean match(POPHandler handler, String line) 
	{
		if (delePattern.matcher(line).matches()) return true;
		else return false;
	}

	public void process(POPHandler handler, String line) 
	throws IOException, POPException 
	{
		if (handler.authenticated)
		{
			Matcher command = delePattern.matcher(line);
			if (command.matches())
			{
				if (handler.messages != null)
				{
					int msgid = Integer.parseInt(command.group(1));
					Message msg = handler.messages.get(msgid);
					if (msg != null)
					{
						msg.setFlag(Message.Flags.DELETED);
						handler.writeLine("+OK");
					}
					else handler.writeLine("-ERR Message not available.");
				}
				else handler.writeLine("-ERR Mailbox not available.");
			}
			else handler.writeLine("-ERR Unrecognized command");
		}
		else handler.writeLine("-ERR Authenticate first.");
	}
}
