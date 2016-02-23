package org.infodancer.engine;

import java.util.concurrent.*;

import org.infodancer.service.ServiceManager;
import org.infodancer.service.api.LifecycleException;
import org.infodancer.service.api.ServiceComponent;

 
public abstract class AbstractServiceEngine extends ScheduledThreadPoolExecutor implements ServiceComponent
{
	protected String name;
	protected ServiceManager manager;
	
	public AbstractServiceEngine(int size, ThreadFactory factory)
	{
		super(size, factory);
	}
		
	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
	
	public void start() throws LifecycleException
	{
		
	}

	public void stop() throws LifecycleException
	{
		try
		{
			super.shutdown();
			super.awaitTermination(30, TimeUnit.SECONDS);
		}
		
		catch (InterruptedException e)
		{
			System.err.println("Shutdown of ServiceEngine " + name + " was interrupted!");
			e.printStackTrace(System.err);
		}
	}

	public void setServiceManager(ServiceManager manager)
	{
		this.manager = manager;
	}

	public ServiceManager getServiceManager()
	{
		return manager;
	}
}
