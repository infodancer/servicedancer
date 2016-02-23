package org.infodancer.service.netstring;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Receives a NetString into a memory buffer.
 * @author mhunter
 *
 */
public class MemoryNetStringReceiver implements NetStringReceiver 
{
	int length;
	java.nio.ByteBuffer buffer;
	
	public MemoryNetStringReceiver(int length)
	{
		this.length = length;
		this.buffer = java.nio.ByteBuffer.allocate(length); 
	}
	
	/**
	 * This method clears the reference to the ByteBuffer associated with this NetStringReceiver.
	 */
	public void close() 
	{
		this.buffer = null;
	}

	public int getLength() 
	{
		return length;
	}

	public int getRemaining() 
	{
		return buffer.remaining();
	}

	public boolean hasRemaining() 
	{
		if (buffer.remaining() > 0) return true;
		else return false;
	}

	public void receive(ByteBuffer input) throws IOException 
	{
		buffer.put(input);
	}
	
	public void receive(ReadableByteChannel input) throws IOException 
	{
		input.read(buffer);
	}
}
