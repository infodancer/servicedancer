package org.infodancer.domain;

import java.util.Hashtable;

import javax.naming.Context;

import org.infodancer.service.api.LifecycleException;
import org.infodancer.service.api.ServiceException;
import org.infodancer.service.api.domain.Domain;

public abstract class AbstractDomain implements Domain, Runnable
{
	protected DomainThreadFactory factory;
	protected Context context;
	protected DomainClassLoader loader;
	protected String name;
	protected Hashtable<?,?> environment; 
	
	public AbstractDomain()
	{
		this.environment = new Hashtable();
	}
	
	protected abstract void initializeDomainContext(Context context) throws ServiceException;
	protected abstract Context createDomainContext() throws ServiceException;
	protected abstract DomainClassLoader createDomainClassLoader(ClassLoader parent) throws ServiceException;
	protected abstract DomainThreadFactory createDomainThreadFactory() throws ServiceException;
	
	public String getDomainName()
	{
		return name;
	}

	public void setDomainName(String name)
	{
		this.name = name;
	}
	
	public synchronized Context getContext()
	{
		return context;
	}
	
	public synchronized ClassLoader getClassLoader()
	{
		return loader;
	}

	/** 
	 * The default implementation initializes the domain information in a separate thread.
	 */
	public void start() throws LifecycleException 
	{
		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		Thread thread = new Thread(this);
		thread.setContextClassLoader(parent);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Initializes the domain information and then exits.
	 */
	public void run()
	{
		try
		{
			ClassLoader parent = Thread.currentThread().getContextClassLoader();
			factory = createDomainThreadFactory();
			loader = createDomainClassLoader(parent);
			context = createDomainContext();
			loader.setContext(context);
			configureThread(Thread.currentThread());
			initializeDomainContext(context);
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public void stop() throws LifecycleException 
	{
	}	
	
	/**
	 * Configures the provided thread to use the domain's classloader and context.
	 * @param thread
	 */
	public void configureThread(Thread thread)
	{
		thread.setContextClassLoader(loader);
	}
}
