package org.infodancer.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.infodancer.service.async.AsyncConnectionImpl;

public class ConnectionInputStream extends InputStream 
{
	AsyncConnectionImpl connection;
	ByteBuffer buffer;
	
	public ConnectionInputStream(AsyncConnectionImpl connection, int size)
	{
		this.connection = connection;
		this.buffer = ByteBuffer.allocateDirect(size);
	}

	public int read() throws IOException
	{
		if (buffer.hasRemaining()) return buffer.get();
		else
		{
			connection.read(buffer);
			return buffer.get();
		}
	}
}
