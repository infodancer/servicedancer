package org.infodancer.service;

/**
 * One of several threads that operate as the core of the ServiceManager's runtime,
 * normally one thread per available processor core.
 * @author matthew
 */
public class ServiceManagerThread extends Thread 
{
	int i;
	
	public ServiceManagerThread(Runnable runnable)
	{
		super(runnable);
		setName("ServiceManagerThread");
	}

	public ServiceManagerThread(Runnable runnable, int i)
	{
		super(runnable);
		this.i = i;
		setName("ServiceManagerThread-" + i);
	}
}
