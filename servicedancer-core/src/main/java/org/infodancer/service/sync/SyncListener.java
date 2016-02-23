package org.infodancer.service.sync;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.infodancer.service.ServiceListener;
import org.infodancer.service.api.ServiceException;
import org.infodancer.service.api.sync.SyncConnection;
import org.infodancer.service.api.sync.SyncService;

public class SyncListener extends ServiceListener
{
	SyncService service;

	public SyncListener(ClassLoader loader, File directory)
	{
		super(loader, directory);
	}

	public void setSyncService(SyncService service)
	{
		this.service = service;
	}
	
	public void start() throws ServiceException
	{
		try
		{
			if (port <= 0)
			{
				throw new ServiceException("Service " + name + " has no port specified!");
			}
			
			if (acceptCount <= 0)
			{
				throw new ServiceException("Service " + name + " has no backlog specified!");	
			}
			
			if (ipaddress == null)
			{
				throw new ServiceException("Service " + name + " has no ip address specified!");
			}
			
			System.out.println("Starting service " + name + " on " + getAddress() + ":" + getPort());
			service.start();
			if (isSSLEnabled()) 
			{
				SSLServerSocketFactory sslfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
				factory = sslfactory;
			}
			else factory = ServerSocketFactory.getDefault(); 
			socket = factory.createServerSocket(port, acceptCount, ipaddress);
			new Thread(this).start();
		}
		
		catch (Exception e)
		{
			throw new ServiceException("Exception while starting service listener!", e);
		}
	}
	
	/**
	 * The ServiceListener thread now handles events for a single server socket.
	 */
	public void run()
	{
		try
		{
			running = true;
			while (running)
			{
				Socket csocket = socket.accept();
				handleAccept(csocket);
			}
		
			running = false;
			try {  socket.close(); } catch (Throwable e) { } 
			try {  service.stop(); } catch (Throwable e) { } 	
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void submit(Callable<SyncConnection> worker)
	{
		workers.submit(worker);
	}
	
	public void stop() throws ServiceException
	{
		running = false;
	}

	private void handleAccept(Socket csocket) throws IOException
	{
		System.out.println("Connection request from " + socket.getInetAddress() + " on port " + socket.getLocalPort());		
		if ((filter(csocket)) && (getMaxConcurrency() > getConnectionCount()))
		{
			SyncConnection connection = new SyncConnectionImpl(csocket, this);
			SyncWorker worker = new SyncWorker(this, service, connection);
			submit(worker);
		}
		else
		{
			System.out.println("Refused connection from " + socket.getInetAddress() + " on port " + socket.getLocalPort());
			try { csocket.close(); } catch (Exception e) { } 									
		}
	}
}
