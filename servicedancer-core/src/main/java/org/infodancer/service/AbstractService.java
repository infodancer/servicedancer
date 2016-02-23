/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.infodancer.handler.ServiceHandlerImpl;
import org.infodancer.service.api.LifecycleException;
import org.infodancer.service.api.async.AsyncService;

public abstract class AbstractService extends AbstractComponent
{
	protected long maxIdleTime;
	protected File tracelogDirectory;
	protected ExecutorService threadpool;
	
	public AbstractService()
	{
		this.maxIdleTime = 300;
	}
	
	public void setMaxIdleTime(long time)
	{
		this.maxIdleTime = time;
	}
	
	public long getMaxIdleTime()
	{
		return maxIdleTime;
	}

	public String getTraceLogDirectory()
	{
		if (tracelogDirectory != null) return tracelogDirectory.getAbsolutePath();
		else return null;
	}
	
	/**
	 * The trace log directory is a directory on the file system which holds a detailed log
	 * of all communication passing over the connections handled by this service.
	 * In this implementation, each connection creates a new file in the trace log directory
	 * named according to the current time in milliseconds, the remote host and port, followed
	 * by the local host and port.
	 * @param directory
	 */
	public void setTraceLogDirectory(String directory)
	{
		tracelogDirectory = new File(directory);
	}
	
	public File createTraceLogFile(ServiceConnectionImpl connection)
	{
		return new File(tracelogDirectory + File.separator + name + "_" + System.currentTimeMillis());
	}

	public Future<?> submit(Callable<?> worker)
	{
		return threadpool.submit(worker);
	}
		
	public void start() throws LifecycleException
	{
		this.threadpool = java.util.concurrent.Executors.newCachedThreadPool();	
	}
	
	public void stop() throws LifecycleException
	{
		this.threadpool.shutdown();
		try { this.threadpool.awaitTermination(maxIdleTime, TimeUnit.SECONDS); }
		catch (Exception e) { e.printStackTrace(); } 
		this.threadpool.shutdownNow();
	}
}
