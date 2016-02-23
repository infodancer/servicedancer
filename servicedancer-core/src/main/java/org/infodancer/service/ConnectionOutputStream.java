package org.infodancer.service;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.infodancer.service.async.AsyncConnectionImpl;

public class ConnectionOutputStream extends java.io.OutputStream 
{
	AsyncConnectionImpl connection;
	ByteBuffer buffer;
	
	public ConnectionOutputStream(AsyncConnectionImpl connection, int size)
	{
		this.connection = connection;
		this.buffer = ByteBuffer.allocateDirect(size);
	}
	
	public void write(int b) throws IOException
	{
		buffer.put((byte) b);
		connection.write(buffer);
	}
}
