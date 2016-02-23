/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

/**
 * Shuts down service handlers in a separate thread.
 **/
 
public class HandlerShutdownWorker implements Runnable
{
	ServiceHandlerWrapper handler;
	
	public HandlerShutdownWorker(ServiceHandlerWrapper handler)
	{
		this.handler = handler;
	}
	
	public void run()
	{
		try
		{
			handler.close();
		}
		
		catch (Exception e)
		{
			System.err.println("Exception while shutting down service!");
			e.printStackTrace();
		}
	}
}