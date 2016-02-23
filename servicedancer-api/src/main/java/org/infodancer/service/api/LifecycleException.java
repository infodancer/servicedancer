package org.infodancer.service.api;

public class LifecycleException extends Exception 
{
	public LifecycleException(Throwable e)
	{
		super(e);
	}

	public LifecycleException(String msg)
	{
		super(msg);
	}
}
