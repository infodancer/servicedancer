package org.infodancer.service.api;

import java.io.IOException;
import java.net.InetAddress;

public interface ServiceConnection 
{
	/**
	 * Provides the InetAddress on which this connection was received (the listening IP).
	 * @return
	 */
	public InetAddress getLocalAddress();
	
	/**
	 * Provides the InetAddress of the remote system.
	 * @return
	 */
	public InetAddress getRemoteAddress();
	
	/**
	 * Provides the port number on the local system (the listening port).
	 * @return
	 */
	public int getLocalPort();
	
	/**
	 * Provides the remote port number.
	 * @return
	 */
	public int getRemotePort();
	
	/**
	 * Provides the local hostname (by reverse DNS lookup on the listening IP address).
	 * @return
	 */
	public String getLocalName();
	
	/**
	 * Provides the remote hostname (by reverse DNS lookup on the remote IP address).
	 * @return
	 */
	public String getRemoteName();
	
	/** 
	 * Provides an InputStream for reading data from the socket.
	 * @return
	 */
	public java.io.InputStream getInputStream() throws IOException;
	
	/**
	 * Provides an outputstream for writing data to the socket.
	 * @return
	 */
	public java.io.OutputStream getOutputStream() throws IOException;

	/**
	 * Indicates whether the connection is closed.
	 * @return
	 */
	public boolean isClosed();
	
	/**
	 * Closes the connection, along with any outstanding streams.
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * Uses the connection-specific logger to write normal log data.  
	 * This method should be used to log data or operations associated with a particular connection, so that debugging can follow that association as well. 
	 * @param data
	 */
	public void log(String data);
	
	/**
	 * Uses the connection-specific logger to write error log data.  
	 * This method should be used to log data or operations associated with a particular connection, so that debugging can follow that association as well. 
	 * @param data
	 */
	public void log(String data, Throwable e);
}
