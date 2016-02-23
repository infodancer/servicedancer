package org.infodancer.msgdancer.pop;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.infodancer.msgdancer.MessageEngine;
import org.infodancer.service.api.LifecycleException;
import org.infodancer.service.api.domain.DomainManager;
import org.infodancer.service.api.sync.SyncConnection;

public class POPService implements org.infodancer.service.api.sync.SyncService
{
	private static final Logger log = Logger.getLogger(POPService.class.getName());
	List<POPCommand> commands;	
	MessageEngine msgengine;
	DomainManager domains;
	String greeting;
		
	public POPService()
	{
		initializeCommands();
	}
	
	public void initializeCommands()
	{
		this.commands = new java.util.ArrayList<POPCommand>();			
		// Load the default commands
		addCommand(new USER());
		addCommand(new PASS());
		addCommand(new LIST());
		addCommand(new RETR());
		addCommand(new STAT());
		addCommand(new UIDL());
		addCommand(new TOP());
		addCommand(new DELE());
		addCommand(new RSET());
		addCommand(new QUIT());
		addCommand(new Unimplemented());
		this.commands = Collections.unmodifiableList(commands);
	}
	
	public void service(SyncConnection connection) throws IOException
	{
		POPHandler handler = new POPHandler(this, connection);
		handler.start();
		handler.run();
	}

	public String getGreeting()
	{
		return greeting;
	}

	public void setGreeting(String greeting)
	{
		this.greeting = greeting;
	}

	private void addCommand(POPCommand command)
	{
		command.setService(this);
		commands.add(command);
	}
	
	public DomainManager getDomainManager()
	{
		return domains;
	}
	
	public void setDomainManager(DomainManager domains)
	{
		this.domains = domains;
	}
	
	public MessageEngine getMessageEngine()
	{
		return msgengine;
	}
	
	public void setMessageEngine(MessageEngine engine)
	{
		this.msgengine = engine;
	}
	
	public synchronized void start() throws LifecycleException
	{
		try
		{
			InitialContext context = new InitialContext();
			msgengine = (MessageEngine) context.lookup(MessageEngine.CONTEXT_MESSAGE_ENGINE); 
			domains = (DomainManager) context.lookup(DomainManager.CONTEXT_DOMAIN_MANAGER);
		}
	
		catch (javax.naming.NamingException e)
		{
			throw new LifecycleException(e);
		}
	}

	public void stop()
	{
		
	}
}
