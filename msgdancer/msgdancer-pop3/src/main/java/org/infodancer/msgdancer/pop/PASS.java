package org.infodancer.msgdancer.pop;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.service.api.domain.Domain;
import org.infodancer.service.api.domain.DomainManager;
import org.infodancer.user.User;
import org.infodancer.user.UserManager;

public class PASS extends AbstractPOPCommand implements POPCommand 
{
	static Pattern passPattern;
	
	static
	{
		passPattern = Pattern.compile("^PASS.(.*)");
	}
	
	public boolean match(POPHandler handler, String line) 
	{
		if (passPattern.matcher(line).matches()) return true;
		else return false;
	}

	public void process(POPHandler handler, String line) 
	throws IOException, POPException 
	{
		try
		{
			Matcher command = passPattern.matcher(line);
			if (command.matches())
			{
				String password = command.group(1);
				if (!handler.authenticated)
				{
					if (handler.user != null)
					{
						String domainName = handler.user.getDomain();
						DomainManager domainManager = service.getDomainManager();
						Domain domain = domainManager.getDomain(domainName);
						UserManager manager = domain.getUserManager();
						User user = manager.authenticate(handler.user.getUser(), password);
						if (user != null)
						{
							handler.msgstore = domain.getMessageStore();
							if (handler.msgstore != null)
							{
								handler.folder = handler.msgstore.getMailbox(handler.user.getUser()); 
								handler.folder.open();
								handler.messages = handler.folder.list();
								handler.authenticated = true;
								handler.writeLine("+OK Authentication succeeded");
							}
							else handler.writeLine("-ERR Authentication failed; msgstore not available");
						}
						else handler.writeLine("-ERR Authentication failed.");
					}
					else handler.writeLine("-ERR USER first.");
				}
				else handler.writeLine("-ERR You are already authenticated.");
			}
			else handler.writeLine("-ERR Unrecognized command");
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			throw new POPException(e);
		}
	}
}
