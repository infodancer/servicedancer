package org.infodancer.msgdancer.pop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.Message;

public class RETR extends AbstractPOPCommand implements POPCommand 
{	
	static Pattern retrPattern;

	static
	{
		retrPattern = Pattern.compile("^RETR.(.*)");
	}

	public boolean match(POPHandler handler, String line) 
	{
		if (retrPattern.matcher(line).matches()) return true;
		else return false;
	}

	public void process(POPHandler handler, String line) 
	throws IOException,	POPException 
	{
		if (handler.authenticated)
		{
			Matcher command = retrPattern.matcher(line);
			if (command.matches())
			{
				if (handler.messages != null)
				{
					BufferedReader reader = null;
					int msgid = Integer.parseInt(command.group(1));
					Message msg = handler.messages.get(msgid);
					handler.writeLine("+OK " + msg.size() + " octects");

					try
					{
						handler.command = this;
						handler.reader = new BufferedReader(new InputStreamReader(msg.getInputStream()));
						String inputLine;
						while ((inputLine = reader.readLine()) != null)
						{
							if (inputLine.startsWith(".")) inputLine = "." + inputLine;
							handler.writeLine(inputLine);
						}
						handler.writeLine(".");
					}
					
					catch (Exception e)
					{
						e.printStackTrace();
						handler.writeLine(".");
					}
				}
				else handler.writeLine("-ERR Mailbox not available.");
			}
			else handler.writeLine("-ERR Unrecognized command");
		}
		else handler.writeLine("-ERR Authenticate first.");
	}
}
