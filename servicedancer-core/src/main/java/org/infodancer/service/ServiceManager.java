/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.io.File;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.InitialContext;

import org.infodancer.service.api.EngineConfig;
import org.infodancer.service.api.ServiceEngine;
import org.infodancer.service.api.ServiceException;
import org.infodancer.service.api.domain.Domain;
import org.infodancer.service.api.domain.DomainManager;

public class ServiceManager implements Runnable
{
	int availableProcessors;
	boolean running;
	long timeout;
	Thread shutdownThread;
	
	DomainManager domainManager;
	ArrayList<SelectionKey> keys = new ArrayList<SelectionKey>();
	Map<String,ServiceEngine> engines = new TreeMap<String,ServiceEngine>();
	Map<String,ServiceListener> listeners = new TreeMap<String,ServiceListener>();
	Map<String,Domain> domains = new TreeMap<String,Domain>();
	// HashMap<SelectionKey,ServiceWorker> workers = new HashMap<SelectionKey,ServiceWorker>();
	LinkedList<ServiceRegistration> registerqueue = new LinkedList<ServiceRegistration>();
	ServiceWorkerPool foreman;
	ServiceManagerThread manager;
	ClassLoader loader;
	File contextFile;
	
	public ServiceManager() throws ServiceException
	{
		this.availableProcessors = Runtime.getRuntime().availableProcessors();
	}

	public long getTimeout() 
	{
		return timeout;
	}

	public void setTimeout(long timeout) 
	{
		this.timeout = timeout;
	}
	
	public void addServiceListener(String name, ServiceListener listener)
	throws ServiceException
	{
		if (!running) listeners.put(name, listener);
		else throw new ServiceException("Cannot add a service to a running ServiceManager");
	}
	
	public ServiceEngine getEngine(String name)
	{
		return engines.get(name);
	}
	
	public void addEngine(String name, ServiceEngine engine) 
	throws ServiceException
	{
		if (!running) engines.put(name, engine);
		else throw new ServiceException("Cannot add an engine to a running ServiceManager");
	}

	public void addDomain(String name, Domain domain) 
	throws ServiceException
	{
		domains.put(name, domain);
	}
		
	public void setDomainManager(DomainManager domainManager)
	{
		this.domainManager = domainManager;
	}
	
	/**
	 * Starts the configured Engines and Services.  
	 * 
	 * @throws ServiceException
	 */
	public void start() throws ServiceException
	{
		int engineCount = 0;
		int serviceCount = 0;
		
		try
		{
			foreman = new ServiceWorkerPool(1);
			InitialContext context = new InitialContext();
			shutdownThread = new ServiceShutdownThread(this);
			if (domainManager != null)
			{
				domainManager.start();
				context.bind(DomainManager.CONTEXT_DOMAIN_MANAGER, domainManager);
			}
			else System.err.println("No domain manager configured!");
			
			synchronized (engines)
			{	
				// First, bind all the engines
				
				for (String name : engines.keySet())
				{
					EngineConfig config = new XMLEngineConfig();
					ServiceEngine engine = (ServiceEngine) engines.get(name);
					System.out.println("Engine " + name + " binding to context...");
					context.bind(name, engine);
					System.out.println("Engine " + name + " initializing...");
					engine.init(config);
					System.out.println("Engine " + name + " starting...");
					engine.start();
					System.out.println("Engine " + name + " started.");
					engineCount++;
				}
			}
	
			synchronized (listeners)
			{	
				for (String name : listeners.keySet())
				{
					ServiceListener listener = listeners.get(name);
					try
					{
						System.out.println("Service " + name + " starting...");
						listener.start();
						serviceCount++;
						context.bind(name, listener);
					}
					
					catch (ServiceException e)
					{
						StringBuffer error = new StringBuffer();
						error.append("Exception while starting service " + name);
						error.append(" on " + listener.getAddress() + ":" + listener.getPort());
						System.err.println(error);
						e.printStackTrace(System.err);
						listener.stop();
					}
				}
			}
			
			System.out.println("Started " + serviceCount + " services and " + engineCount + " engines.");
			if ((serviceCount > 0) || (engineCount > 0))
			{
				running = true;
				manager = new ServiceManagerThread(this);
				manager.start();
			}
			else System.out.println("No services or engines running; exiting...");
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ServiceException(e);
		}
	}
		
	/**
	 * The ServiceManager thread now handles events for all listening sockets.
	 */
	public void run()
	{
		while (running)
		{
			try
			{
				Thread.sleep(100000);
				Thread.yield();
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}


	public void stop()
	{
		synchronized (listeners)
		{
			
			for (String name : listeners.keySet())
			{
				try
				{
					System.out.println("Shutting down " + name);
					ServiceListener listener = listeners.get(name);
					listener.stop();
					System.out.println("Shutdown of service " + name + " complete.");
				}
				
				catch (Throwable e)
				{
					System.err.println("Exception while shutting down service listener " + name);
					e.printStackTrace();
				}				
			}
		}
		
		synchronized (engines)
		{
			
			for (String name : engines.keySet())
			{
				try
				{
					System.out.println("Shutting down engine " + name);
					ServiceEngine engine = engines.get(name);
					engine.stop();
					System.out.println("Shutdown of engine " + name + " complete.");
				}
				
				catch (Throwable e)
				{
					System.err.println("Exception while shutting down engine " + name);
					e.printStackTrace();
				}
			}
		}
		
		foreman.shutdown();
	}
	
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("ServiceManager[" + availableProcessors + "]");
		return buffer.toString();
	}
	
	void setServerClassLoader(ClassLoader loader)
	{
		this.loader = loader;
	}

	public File getContextFile()
	{
		return contextFile;
	}
	
	public void setContextFile(File contextFile)
	{
		this.contextFile = contextFile;
	}
}
