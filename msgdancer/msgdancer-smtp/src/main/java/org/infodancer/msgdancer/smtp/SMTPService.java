package org.infodancer.msgdancer.smtp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.infodancer.message.EmailAddress;
import org.infodancer.msgdancer.MessageEngine;
import org.infodancer.service.api.LifecycleException;
import org.infodancer.service.api.ServiceConnection;
import org.infodancer.service.api.domain.DomainManager;
import org.infodancer.service.api.sync.SyncConnection;

public class SMTPService implements org.infodancer.service.api.sync.SyncService
{
	private static final Logger log = Logger.getLogger(SMTPService.class.getName());
	List<ServiceConnection> connections = new ArrayList<ServiceConnection>();
	List<SMTPCommandHandler> commands;	
	MessageEngine msgengine;
	DomainManager domains;
	private int size;
	
	private String spamc;
	private String spamOptions;
	private boolean spamByUser;
	private String spamUser;
	
	private String greeting;
	private boolean validateUsers;
	private ProcessBuilder spambuilder;
	
	public SMTPService()
	{
		initializeCommands();
	}

	public SMTPService(MessageEngine msgengine, DomainManager domains)
	{
		this.msgengine = msgengine;
		this.domains = domains;
		initializeCommands();
	}

	private void initializeCommands()
	{
		this.commands = new java.util.ArrayList<SMTPCommandHandler>();			
		// Load the default commands			
		addCommand(new HELO());
		addCommand(new EHLO());
		addCommand(new AUTH_PLAIN());
		addCommand(new AUTH_LOGIN());
		addCommand(new MAILFROM());
		addCommand(new RCPTTO());
		addCommand(new DATA());
		addCommand(new VRFY());
		addCommand(new RSET());
		addCommand(new NOOP());
		addCommand(new QUIT());
		this.commands = Collections.unmodifiableList(commands);		
	}
	
	/**
	 * Specifies the maximum size, in bytes, of a message that can be transmitted over smtp.
	 * @return size in bytes; defaults to 0.
	 */
	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	/**
	 * The options to pass to spamc, aside from the --username option.
	 * @return
	 */
	public String getSpamOptions()
	{
		return spamOptions;
	}

	public void setSpamOptions(String spamOptions)
	{
		this.spamOptions = spamOptions;
	}

	/**
	 * Whether to invoke spamc for each recipient individually.
	 * @return
	 */
	public boolean isSpamByUser()
	{
		return spamByUser;
	}

	public void setSpamByUser(boolean spamByUser)
	{
		this.spamByUser = spamByUser;
	}

	/**
	 * The content of the --username option to spamc; implies isSpamByUser() false;
	 * @return
	 */
	public String getSpamUser()
	{
		return spamUser;
	}

	public void setSpamUser(String spamUser)
	{
		this.spamUser = spamUser;
	}

	/**
	 * The location of the spamc binary.  
	 * @return
	 */
	public String getSpamc()
	{
		return spamc;
	}

	public void setSpamc(String spamc)
	{
		this.spamc = spamc;
	}

	public boolean isValidateUsers()
	{
		return validateUsers;
	}

	public void setValidateUsers(boolean validateUsers)
	{
		this.validateUsers = validateUsers;
	}

	public String getGreeting() 
	{
		return greeting;
	}

	public void setGreeting(String greeting) 
	{
		this.greeting = greeting;
	}

	public synchronized void stop()
	{
		for (ServiceConnection connection : connections)
		{
			try
			{
				connection.close();
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void service(SyncConnection connection) throws IOException
	{
		SMTPProtocol protocol = new SMTPProtocol(this, connection);
		protocol.start();
		protocol.run();
	}
	
	private void addCommand(SMTPCommandHandler command)
	{
		commands.add(command);
	}
	
	public synchronized void start() throws LifecycleException
	{
		try
		{
			InitialContext context = new InitialContext();
			// Null check here is to enable debugging to insert values without a context
			if (msgengine == null) msgengine = (MessageEngine) context.lookup(MessageEngine.CONTEXT_MESSAGE_ENGINE);
			if (domains == null) domains = (DomainManager) context.lookup(DomainManager.CONTEXT_DOMAIN_MANAGER);
		}
	
		catch (javax.naming.NamingException e)
		{
			throw new LifecycleException(e);
		}
	}

	public MessageEngine getMessageEngine()
	{
		return msgengine;
	}
	
	public void setMessageEngine(MessageEngine msgengine)
	{
		this.msgengine = msgengine;
	}
	
	public DomainManager getDomainManager()
	{
		return domains;
	}
	
	public void setDomainManager(DomainManager domains)
	{
		this.domains = domains;
	}
	
	public String mxlookup(String name)
	{
		return null;
	}	
	
	/**
	 * If a spam command is defined to invoke SpamAssassin, call it.
	 * On exception, the original message will be returned.
	 * @param recipient
	 * @param msgdata
	 * @return
	 */
	public byte[] processSpamCheck(EmailAddress recipient, byte[] msgdata)
	{
		InputStream input = null;
		OutputStream output = null;
		
		try
		{
			if (spamc != null)
			{
				byte[] buffer = new byte[32768];
				List<String> cmd = new ArrayList<String>();
				cmd.add(getSpamc());
				if ((spamUser != null) && (spamUser.trim().length() > 0))
				{
					cmd.add("--username=" + spamUser);
				}
				else if (spamByUser)
				{
					cmd.add("--username=" + recipient.getUser());
				}
				if ((spamOptions != null) && (spamOptions.trim().length() >= 0))
				{
					String[] args = spamOptions.split(" ");
					for (String arg : args)
					{
						cmd.add(arg);
					}
				}
				ProcessBuilder pb = new ProcessBuilder(cmd.toArray(new String[cmd.size()]));
				Process p = pb.start();
				input = p.getInputStream();
				output = p.getOutputStream();
				output.write(msgdata);
				output.close();
				
				ByteArrayOutputStream outdata = new ByteArrayOutputStream();
				int length = 0;
				while ((length = input.read(buffer)) > 0)
				{
					outdata.write(buffer, 0, length);
				}
				return outdata.toByteArray();
			}
			else return msgdata;
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return msgdata;
		}
		
		finally
		{
			try { if (input != null) input.close(); } catch (Exception e) { } 
			try { if (output != null) output.close(); } catch (Exception e) { } 
		}
	}

	public byte[] crypt(Object key, byte[] msgdata)
	{
		return msgdata;
	}

	public Object findPublicKey(EmailAddress recipient)
	{
		return null;
	}

}
