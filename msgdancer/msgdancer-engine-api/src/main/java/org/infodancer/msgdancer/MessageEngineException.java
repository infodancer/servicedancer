package org.infodancer.msgdancer;

public class MessageEngineException extends Exception
{
	boolean permanent;

	public MessageEngineException(String msg, Exception e)
	{
		super(msg, e);
		this.permanent = false;
	}

	public MessageEngineException(String msg)
	{
		super(msg);
		this.permanent = false;
	}
	
	public MessageEngineException(Exception e)
	{
		super(e);
	}

	public boolean isPermanent()
	{
		return permanent;
	}
}
