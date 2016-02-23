/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.nio.channels.*;

public class ServiceWorker implements java.util.concurrent.Callable<ServiceHandlerWrapper>
{
	private static long identifier = 0;
	private long id;
	private ServiceHandlerWrapper handler;
	private SelectionKey key;
	
	public ServiceWorker(SelectionKey key, ServiceHandlerWrapper handler)
	{
		this.id = identifier++; 
		this.handler = handler;
		this.key = key;
	}
	
	public SelectionKey getSelectionKey()
	{
		return key;
	}
	
	public ServiceHandlerWrapper getServiceHandlerWrapper()
	{
		return handler;
	}
	
	public ServiceHandlerWrapper call() throws Exception
	{
		synchronized (handler)
		{
			synchronized (key)
			{
				try { handler.service(); } catch (Exception e) { e.printStackTrace(); } 
				handler.removeWorker(this);
				return handler;
			}
		}
	}

	public long getIdentifier() 
	{
		return id;
	}
}
