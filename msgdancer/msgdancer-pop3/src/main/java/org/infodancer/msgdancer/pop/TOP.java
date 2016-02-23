package org.infodancer.msgdancer.pop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.Message;

public class TOP extends AbstractPOPCommand implements POPCommand 
{
	static Pattern topPattern;

	static
	{
		topPattern = Pattern.compile("^TOP (.*) (.*)");
	}

	public boolean match(POPHandler handler, String line) 
	{
		if (topPattern.matcher(line).matches()) return true;
		else return false;
	}

	public void process(POPHandler handler, String line) 
	throws IOException,	POPException 
	{
		if (handler.authenticated)
		{
			Matcher command = topPattern.matcher(line);
			if (command.matches())
			{
				if (handler.messages != null)
				{
					BufferedReader reader = null;
					int msgid = Integer.parseInt(command.group(1));
					int lines = Integer.parseInt(command.group(2));
					Message msg = handler.messages.get(msgid);
					handler.writeLine("+OK");

					try
					{
						boolean headers = true;
						reader = new BufferedReader(new InputStreamReader(msg.getInputStream()));
						String inputLine;
						while ((inputLine = reader.readLine()) != null)
						{
							if (inputLine.startsWith(".")) inputLine = "." + inputLine;
							if ((inputLine.length() == 0) && (headers)) headers = false;
							handler.writeLine(inputLine);
							if (!headers) lines--;
							if (lines <= 0) break;
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
