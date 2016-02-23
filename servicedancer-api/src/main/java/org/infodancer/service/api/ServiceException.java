package org.infodancer.service.api;

/**

 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

public class ServiceException extends Exception
{	
	public ServiceException(String msg)
	{
		super(msg);
	}
	
	public ServiceException(Exception e)
	{
		super(e);
	}

	public ServiceException(String msg, Exception e)
	{
		super(msg, e);
	}
}
