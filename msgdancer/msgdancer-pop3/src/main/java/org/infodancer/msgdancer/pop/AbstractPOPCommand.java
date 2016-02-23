package org.infodancer.msgdancer.pop;

import java.io.IOException;

import org.infodancer.msgdancer.MessageEngine;

public abstract class AbstractPOPCommand implements POPCommand 
{
	POPService service;
	MessageEngine engine;
	
	public abstract boolean match(POPHandler handler, String line);

	public abstract void process(POPHandler handler, String line) 
	throws IOException, POPException;

	public POPService getService()
	{
		return service;
	}

	public void setService(POPService service)
	{
		this.service = service;
	}

	public MessageEngine getMessageEngine()
	{
		return engine;
	}

	public void setMessageEngine(MessageEngine engine)
	{
		this.engine = engine;
	} 
}
