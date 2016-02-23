package org.infodancer.service.api.sync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import org.infodancer.service.api.sync.SyncConnection;

/**
 * A class to help with writing test cases for services.
 * @author matthew
 *
 */
public class TestSyncConnection implements SyncConnection
{
	int localPort;
	int remotePort;
	String localName;
	String remoteName;
	InetAddress localAddress;
	InetAddress remoteAddress;
	boolean closed = false;
	InputStream input;
	OutputStream output;
	boolean ssl;
	
	public TestSyncConnection()
	{
		localPort = 0;
		remotePort = 0;
		localName = "localhost";
		remoteName = "localhost";
	}
	
	public InputStream getInputStream() throws IOException
	{
		return input;
	}

	public void setInputStream(InputStream input)
	{
		this.input = input;
	}

	public OutputStream getOutputStream() throws IOException
	{
		return output;
	}

	public void setOutputStream(OutputStream output)
	{
		this.output = output;
	}

	public void setLocalPort(int localPort)
	{
		this.localPort = localPort;
	}

	public void setRemotePort(int remotePort)
	{
		this.remotePort = remotePort;
	}

	public void setLocalName(String localName)
	{
		this.localName = localName;
	}

	public void setRemoteName(String remoteName)
	{
		this.remoteName = remoteName;
	}

	public void setLocalAddress(InetAddress localAddress)
	{
		this.localAddress = localAddress;
	}

	public void setRemoteAddress(InetAddress remoteAddress)
	{
		this.remoteAddress = remoteAddress;
	}

	public void setClosed(boolean closed)
	{
		this.closed = closed;
	}

	@Override
	public InetAddress getLocalAddress()
	{
		return localAddress;
	}

	@Override
	public InetAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	@Override
	public int getLocalPort()
	{
		// TODO Auto-generated method stub
		return localPort;
	}

	@Override
	public int getRemotePort()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLocalName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isClosed()
	{
		return closed;
	}

	@Override
	public void close() throws IOException
	{
		closed = true;
	}

	@Override
	public void log(String data)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void log(String data, Throwable e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean startTLS() throws IOException
	{
		this.ssl = true;
		return ssl;
	}

	@Override
	public boolean isSSLEnabled()
	{	
		return ssl;
	}
}
