/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.infodancer.service.api.ServiceConnection;

public abstract class ServiceConnectionImpl implements ServiceConnection
{
	protected static long connectionId = 0;
	protected long lastEventTime = System.currentTimeMillis();;
	long id;
	/** Stores the buffer size for the input and output streams **/
	protected int buffersize;
	protected Socket socket;
	protected SSLSocket sslSocket;
	protected SSLSocketFactory sslSocketFactory;
	private Logger log;
		
	public ServiceConnectionImpl()
	{
		this.id = connectionId++;
		this.log = Logger.getLogger("[" + connectionId + "] ");				
	}
		
	public int getLocalPort()
	{
		return socket.getLocalPort();
	}
	
	public int getRemotePort()
	{
		return socket.getPort();
	}
	
	public String getLocalName()
	{
		java.net.InetAddress localAddress = socket.getLocalAddress(); 
		return localAddress.getCanonicalHostName();
	}
	
	@Override
	public InetAddress getLocalAddress()
	{
		return socket.getLocalAddress();
	}

	@Override
	public InetAddress getRemoteAddress()
	{
		return socket.getInetAddress();
	}

	@Override
	public String getRemoteName()
	{
		java.net.InetAddress localAddress = socket.getInetAddress(); 
		return localAddress.getCanonicalHostName();
	}		
	
	public boolean isConnected()
	{
		return socket.isConnected();
	}
	
	/** 
	 * Provides the remote Subject for this connection.
	 * @return a Subject that represents the remote system or user.
	 **/
	
	/*
	public Subject getSubject()
	{
		return subject;
	}
	*/
	
	public boolean isClosed()
	{
		return socket.isClosed();
	}
	
	public void close() throws java.io.IOException
	{
		lastEventTime = System.currentTimeMillis();
		if (socket != null) socket.close();
	}
	
	public String getServerName()
	{
		try
		{
			return socket.getLocalAddress().getHostName();
		}
		
		catch (Exception e)
		{
			return "unknown";
		}
	}
		
	/**
	 * Uses the connection-specific logger to write normal log data.  
	 * This method should be used to log data or operations associated with a particular connection, so that debugging can follow that association as well. 
	 * @param data
	 */
	public void log(String data)
	{
		log.finer(data);
	}
	
	/**
	 * Uses the connection-specific logger to write error log data.  
	 * This method should be used to log data or operations associated with a particular connection, so that debugging can follow that association as well. 
	 * @param data
	 */
	public void log(String data, Throwable e)
	{
		e.printStackTrace(System.err);
		log.warning(data);
	}
	
	public void setLogger(Logger log)
	{
		this.log = log;
	}
	
	/** 
	 * Provides an InputStream for reading data from the socket.
	 * @return
	 */
	public abstract java.io.InputStream getInputStream() throws IOException;
	
	/**
	 * Provides an outputstream for writing data to the socket.
	 * @return
	 */
	public abstract java.io.OutputStream getOutputStream() throws IOException;
	
	/** 
	 * Compares the connection id value.
	 */
	public boolean equals(Object o)
	{
		if (o instanceof ServiceConnectionImpl)
		{
			ServiceConnectionImpl sc = (ServiceConnectionImpl) o;
			System.out.println("this.id: " + this.id);
			System.out.println("sc.id: " + sc.id);
			if (this.id == sc.id) return true;
			else return false;
		}
		else return false;
	}
	
	public long getLastEventTime()
	{
		return lastEventTime;
	}
}