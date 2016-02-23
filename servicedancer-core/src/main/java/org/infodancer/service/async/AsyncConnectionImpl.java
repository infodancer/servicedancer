package org.infodancer.service.async;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import org.infodancer.service.ConnectionInputStream;
import org.infodancer.service.ConnectionOutputStream;
import org.infodancer.service.ServiceConnectionImpl;
import org.infodancer.service.api.async.AsyncConnection;

public class AsyncConnectionImpl extends ServiceConnectionImpl implements ByteChannel,AsyncConnection
{
	SSLEngine sslEngine;
	SocketChannel channel;
	AsyncListener listener;
	
	public AsyncConnectionImpl(SocketChannel channel, AsyncListener listener)
	{
		super();
		this.channel = channel;
		this.socket = channel.socket();
		this.listener = listener;
	}
	
	public AsyncConnectionImpl(SocketChannel channel, AsyncListener listener, SSLEngine sslEngine)
	{
		this(channel, listener);
		this.sslEngine = sslEngine;
	}

	public void setSSLEngine(SSLEngine sslEngine)
	{
		this.sslEngine = sslEngine;
	}

	public SSLEngine getSSLEngine()
	{
		return sslEngine;
	}

	/**
	 * Provides a reference to the SocketChannel underlying this connection.  This will bypass any 
	 * processing done in the ServiceConnection's methods (such as debugging trace output!), and should
	 * not be used for IO, only to add or remove from a selector. 
	 * @return A reference to the underlying SocketChannel.
	 */
	
	public SocketChannel getSocketChannel()
	{
		return channel;
	}

	public boolean isOpen()
	{
		boolean result = channel.isOpen(); 
		return result;
	}
	
	public void close() throws java.io.IOException
	{
		if (channel != null) channel.close();
		listener.removeConnection(this);
		super.close();
	}
	
	public int write(ByteBuffer src) throws IOException
	{
		lastEventTime = System.currentTimeMillis();
		int result = channel.write(src);
		return result;
	}

	public int read(ByteBuffer dst) throws IOException
	{
		lastEventTime = System.currentTimeMillis();
		int result = channel.read(dst);
		return result;
	}
	
	/** 
	 * Provides an InputStream for reading data from the socket.
	 * @return
	 */
	public java.io.InputStream getInputStream()
	{
		return new ConnectionInputStream(this, buffersize);
	}
	
	/**
	 * Provides an outputstream for writing data to the socket.
	 * @return
	 */
	public java.io.OutputStream getOutputStream()
	{
		return new ConnectionOutputStream(this, buffersize);
	}
}
