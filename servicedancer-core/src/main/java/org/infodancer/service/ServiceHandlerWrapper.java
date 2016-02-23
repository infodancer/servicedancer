/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.infodancer.handler.ServiceHandlerImpl;
import org.infodancer.service.api.async.AsyncService;

/** 
 * Wraps a ServiceHandler with additional information necessary to the manager, 
 * such as connection idle time.
 **/

public class ServiceHandlerWrapper
{
	private final static Logger log = Logger.getLogger(ServiceHandlerWrapper.class.getName());
	long lastEventTime = System.currentTimeMillis();;
	boolean stopping = false;
	ServiceHandlerImpl handler;
	ServiceListener listener;
	SelectionKey selectionKey;
	List<ServiceWorker> workers = new LinkedList<ServiceWorker>();	
	
	public ServiceHandlerWrapper(ServiceListener listener, SelectionKey selectionKey, ServiceHandlerImpl handler)
	{
		this.handler = handler;
		this.listener = listener;
		this.selectionKey = selectionKey;
		int ops = handler.interestOps();
		selectionKey.interestOps(ops);
		selectionKey.selector().wakeup();
	}
	
	/** 
	 * Provides the last event time, in milliseconds.
	 **/
	 
	public long getLastEventTime()
	{
		return lastEventTime;	
	}
	
	public AsyncService getService()
	{
		return handler.getService();
	}
	
	public ServiceHandlerImpl getServiceHandler()
	{
		return handler;
	}
		
	public void service() throws java.io.IOException
	{
		if (selectionKey.isValid())
		{
			lastEventTime = System.currentTimeMillis();	
			handler.service();
			int ops = handler.interestOps();
			selectionKey.interestOps(ops);
			selectionKey.selector().wakeup();
		}
	}

	public boolean isValid()
	{
		if (handler != null) return handler.isValid();
		else return false;
	}
	
	public void close() throws IOException
	{
		handler.close();
	}

	public ServiceManager getServiceManager()
	{
		return handler.getServiceManager();
	}

	public void setServiceManager(ServiceManager manager)
	{
		handler.setServiceManager(manager);
	}

	public ServiceHandlerImpl getHandler()
	{
		return handler;
	}

	public void setHandler(ServiceHandlerImpl handler)
	{
		this.handler = handler;
	}

	public SelectionKey getSelectionKey()
	{
		return selectionKey;
	}

	public void setSelectionKey(SelectionKey selectionKey)
	{
		this.selectionKey = selectionKey;
	}

	public void setLastEventTime(long lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public void setShuttingDown(boolean stopping) 
	{
		this.stopping = stopping;
	}

	public boolean isShuttingDown() 
	{
		return stopping;
	}

	public void addWorker(ServiceWorker worker) 
	{
		synchronized (workers)
		{
			this.workers.add(worker);
		}
	}
	
	public void removeWorker(ServiceWorker worker)
	{
		synchronized (workers)
		{
			this.workers.remove(worker);
		}
	}

	public int workerCount() 
	{
		synchronized (workers)
		{
			return this.workers.size();
		}
	}
}