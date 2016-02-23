/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.infodancer.service.api.Service;
import org.infodancer.service.api.async.ServiceHandler;

/** 
 * Provides basic utility functions for a ServiceHandler.
 **/
 
public abstract class AbstractServiceHandler implements ServiceHandler
{
	protected boolean closed;
	protected Service service;
	protected ServiceManager manager;
	protected File tracelog;
	protected BufferedWriter tracelogWriter;
	
	public AbstractServiceHandler(Service service)
	{
		this.closed = false;
		this.service = service;
	}

	public boolean isValid()
	{
		if (closed) return false;
		else return true;
	}
	
	public void setService(Service service)
	{
		this.service = service;
	}
		
	public ServiceManager getServiceManager()
	{
		return manager;
	}

	public Service getService()
	{
		return service;
	}

	public void setTraceLog(File tracelog)
	{
		this.tracelog = tracelog;
	}
	
	public File getTraceLog()
	{
		return tracelog;
	}

	/**
	 * Starts the tracelogWriter if the tracelog file is not null.
	 */
	public void start() throws IOException
	{
		if (tracelog != null)
		{
			tracelogWriter = new BufferedWriter(new FileWriter(tracelog));
		}
	}

	public void setServiceManager(ServiceManager manager)
	{
		this.manager = manager;
	}

	/** 
	 * The read() method should be overridden by subclasses.
	 */
	public abstract void read() throws java.io.IOException;
	
	/** 
	 * The service() method should be overridden by subclasses. 
	 */
	public abstract ServiceHandler service() throws java.io.IOException;

	/** 
	 * The write() method should be overridden by subclasses.
	 */
	public abstract void write() throws java.io.IOException;

	public void stop() throws IOException
	{
		close();
	}
	
	/**
	 * Closes the tracelogWriter if it was open, and marks this handler as closed.
	 */
	public void close() throws java.io.IOException
	{
		this.closed = true;
		try { if (tracelogWriter != null) tracelogWriter.close(); } 
		catch (Exception e) { } 
	}
}
