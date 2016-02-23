/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;

import org.infodancer.service.api.ServiceException;
import org.infodancer.service.api.filter.ServiceFilter;

public abstract class ServiceListener implements Runnable
{
	protected String name;
	protected File directory;
	protected ClassLoader loader;
	
	protected boolean sslEnabled;
	protected boolean running;
	protected ServerSocket socket;
	protected LinkedList<ServiceFilter> filters = new LinkedList<ServiceFilter>();
	protected LinkedList<ServiceConnectionImpl> connections = new LinkedList<ServiceConnectionImpl>();
	protected ExecutorService workers = Executors.newCachedThreadPool(new ServiceListenerThreadFactory());
	
	// Service Variables
	protected int port;
	protected int timeout;
	protected int threads;

	/**
	 * acceptCount defines the number of connections waiting to be processed. 
	 * When there are more connections waiting than this number, additional 
	 * connection attempts will be dropped.  The default is 32.
	 */
	protected int acceptCount = 32;

	/**
	 * Concurrency defines the number of simultaneous connections this listener 
	 * will permit.  The default is 32.
	 */
	protected int maxConcurrency = 32;
	protected String protocol;
	protected String className;
	protected InetAddress ipaddress;
	protected ServerSocketFactory factory;
	protected String keystoreFile;
	protected String keystorePass;
	
	// Abstract methods
	public abstract void start() throws ServiceException;
	public abstract void run();
	public abstract void stop() throws ServiceException;
	
	public ServiceListener(ClassLoader loader, File directory)
	{
		this.loader = loader;
		this.directory = directory;
		this.name = directory.getName();
	}
	
	public String getKeystoreFile()
	{
		return keystoreFile;
	}
	
	public void setKeystoreFile(String keystoreFile)
	{
		this.keystoreFile = keystoreFile;
	}
	
	public String getKeystorePass()
	{
		return keystorePass;
	}
	
	public void setKeystorePass(String keystorePass)
	{
		this.keystorePass = keystorePass;
	}
	public int getPort()
	{
		return port;
	}
	
	/** 
	 * Checks the provided Socket against the Service's configured filters, 
	 * returning a boolean that indicates whether the connection should be accepted or not.
	 * @return true if the connection should be accepted, false if rejected.
	 */
	
	public boolean filter(Socket socket)
	{
		return true;
	}
	
	public boolean isSSLEnabled()
	{
		return sslEnabled;
	}
	
	public void setSSLEnabled(boolean enabled)
	{
		this.sslEnabled = enabled;
	}
	public String getProtocol() 
	{
		return protocol;
	}

	public void setProtocol(String protocol) 
	{
		this.protocol = protocol;
	}

	public InetAddress getAddress() 
	{
		return ipaddress;
	}

	public void setAddress(InetAddress ipaddress) 
	{
		this.ipaddress = ipaddress;
	}
	
	public void setAddress(String ipaddress) throws UnknownHostException 
	{
		this.ipaddress = InetAddress.getByName(ipaddress);
	}
	
	public int getTimeout() 
	{
		return timeout;
	}

	public void setTimeout(int timeout) 
	{
		this.timeout = timeout;
	}

	public void setPort(int port) 
	{
		this.port = port;
	}

	public int getConnectionCount() 
	{
		return connections.size();
	}

	public int getMaxConcurrency() 
	{
		return maxConcurrency;
	}
	
	public void setMaxConcurrency(int maxConcurrency)
	{
		this.maxConcurrency = maxConcurrency;
	}
	
	public synchronized void addConnection(ServiceConnectionImpl connection)
	{
		System.out.println("ADDING CONNECTION[" + connections.size() + " total]");
		connections.add(connection);
		System.out.println("ADDED CONNECTION[" + connections.size() + " total]");
	}
	
	private java.util.List<Long> removedConnections = new java.util.LinkedList<Long>();
	public synchronized void removeConnection(ServiceConnectionImpl connection) 
	{
		if (removedConnections.contains(connection.id))
		{
			System.out.println("Tried to remove a connection twice (id: " + connection.id + ")");
		}
		else removedConnections.add(connection.id);
		System.out.println("REMOVING CONNECTION[" + connections.size() + " total]");
		connections.remove(connection);
		System.out.println("REMOVED CONNECTION[" + connections.size() + " total]");
	}

	public ClassLoader getClassLoader()
	{
		return loader;
	}
	
	public void setClassLoader(ClassLoader loader)
	{
		this.loader = loader;
	}
}
