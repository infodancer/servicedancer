/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

public class ServiceShutdownThread extends Thread
{
	ServiceManager manager;
	
	public ServiceShutdownThread(ServiceManager manager) 
	throws SecurityException, IllegalStateException, IllegalArgumentException
	{
		this.manager = manager;
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(this);
		setName("ServiceManager-shutdown-thread");
	}
	
	public void run()
	{
		manager.stop();
	}
}
