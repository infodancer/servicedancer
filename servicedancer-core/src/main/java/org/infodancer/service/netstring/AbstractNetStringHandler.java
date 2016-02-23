package org.infodancer.service.netstring;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import javax.net.ssl.SSLEngine;

import org.infodancer.handler.AbstractServiceHandler;
import org.infodancer.service.ServiceConnectionImpl;
import org.infodancer.service.api.async.AsyncService;
import org.infodancer.service.async.AsyncConnectionImpl;

/**
 * The AbstractNetStringHandler implements a NetString-based protocol.
 * (See DJB's http://cr.yp.to/proto/netstrings.txt for details on netstrings).
 * It is intended to be used as a lower-layer protocol to enable higher-level
 * protocols based on the exchange of formatted data (such as XML) within the 
 * protocol itself.  This provides significantly better capabilities for data
 * exchange.
 * @author mhunter
 *
 */

public abstract class AbstractNetStringHandler extends AbstractServiceHandler 
{	
	public static final int BUFFER_SIZE = 32768;
	protected Charset charset = Charset.forName("US-ASCII");
	protected CharsetDecoder decoder = charset.newDecoder();
	protected CharsetEncoder encoder = charset.newEncoder();
	protected ByteBuffer inputBuffer;
	protected ByteBuffer outputBuffer;
	protected AsyncConnectionImpl connection;
	protected SSLEngine sslEngine;
	/** Data for incoming NetStrings is written here **/
	protected NetStringReceiver receiver;
	
	public AbstractNetStringHandler(AsyncService service, AsyncConnectionImpl connection)
	{
		super(service);
		this.closed = false;
		this.connection = connection;
		// this.sslEngine = connection.getSSLEngine();
		this.inputBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		this.outputBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		inputBuffer.clear();
		outputBuffer.clear();
	}	

	public void sendMessage(String message) throws java.io.IOException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(message.length());
		buffer.append(':');
		buffer.append(message);
		buffer.append(',');
		write(buffer.toString());
	}

	/**
	 * This method will be called once per NetString received.
	 */
	protected abstract void service(String msg);
	
	/**
	 * This method will be called with each NetString received.  The default implementation 
	 * converts the NetString to a CharBuffer and calls the service(CharBuffer) method.
	 * If a particular handler wishes to see the ByteBuffer version of incoming data, it can
	 * override this method.
	 * @param msg
	 */
	protected synchronized void service(ByteBuffer msg) throws java.io.IOException
	{
		if (msg != null) service(decoder.decode(msg));
	}

	/**
	 * This method will be called with each NetString received.  The default implementation 
	 * converts the CharBuffer to a String and calls the service(String) method.
	 * If a particular handler wishes to see the CharBuffer version of incoming data, it can
	 * override this method.
	 * @param msg
	 */
	protected synchronized void service(CharBuffer msg)
	{
		if (msg != null) service(msg.toString());
	}

	/**
	 * When the beginning of a NetString is detected, this method is called to read the body of 
	 * the NetString.  The default implementation uses the receiver variable to write data efficiently.  
	 * Data that is already buffered will be copied into the receiver, and any data on subsequent reads
	 * will be transferred directly (if the receiver supports that). 
	 * @throws java.io.IOException
	 */
	protected synchronized void readData() throws java.io.IOException
	{
		if (receiver != null)
		{
			// Either of the following lines may read nothing at all
			// The receiver knows how much input it needs to complete
			receiver.receive(inputBuffer);
			receiver.receive(connection);
			if (receiver.getRemaining() == 0)
			{
				receiver.close();
			}
		}
	}
	
	/**
	 * Creates a NetStringReceiver to handle the current incoming NetString.
	 * Typically NetStrings will be either buffered in memory or written to a file
	 * with direct IO.  Which one is appropriate depends on the details of the protocol.
	 * The default implementation creates a MemoryNetStringReceiver.  Subclasses
	 * should override this method and make their own determination if they decide to use
	 * direct IO.
	 * @param length
	 */
	protected NetStringReceiver createNetStringReceiver(int length)
	{
		return new MemoryNetStringReceiver(length);  
	}
	
	/** 
	 * Reads data from the SocketChannel and 
	 **/

	protected synchronized int readNetString() throws java.io.IOException
	{
		if (receiver != null)
		{
			if (receiver.hasRemaining()) receiver.receive(connection);
		}
		
		// Read the available data into the input buffer
		int result = connection.read(inputBuffer);
		inputBuffer.flip();
		
		// Are we already in a netstring?
		if (receiver != null) readData();
		
		// If we aren't in a NetString (even if we were a moment ago)
		if (receiver == null)
		{
			// Scan for the beginning of a netstring
			int b = -1;
			int limit = inputBuffer.limit();
			int position = inputBuffer.position();
			
			// Iterate through the new data received
			for (int count = position; count < limit; count++)
			{
				b = (int) inputBuffer.get(count);			
				if (b == ':')
				{
					// Duplicate the buffer so we can manipulate it
					ByteBuffer temp = inputBuffer.duplicate();
					
					// Hide the ':' and any further data from the length decode
					temp.position(position);
					temp.limit(count);
					
					// Decode the length value, plus the terminating ','
					String slength = decoder.decode(temp).toString();
					if (slength != null)
					{
						int length = Integer.parseInt(slength) + 1; 
						
						// Create a new buffer for the incoming NetString
						receiver = createNetStringReceiver(length);
		
						// Position the buffer to start at the beginning of the data
						position = count + 1;
						inputBuffer.position(position);
						
						// Read as much of the NetString as we can
						readData();

						// Start processing again at the current position
						position = inputBuffer.position();
						count = position;
					}
					else throw new NetStringProtocolException("No length data found!");
				}
			}
			
			// Reset the position to the beginning of the last 
			inputBuffer.position(position);
		}			

		inputBuffer.compact();
		return result;
	}	
	
	/**
	 * We override the service() method in order to differentiate between reads 
	 * that go into the line buffer and reads that are part of a NetString. 
	 **/

	public synchronized void service() throws java.io.IOException
	{
		read();
		write();
	}
	
	public synchronized ByteBuffer encode(String value) throws CharacterCodingException
	{
		return encode(CharBuffer.wrap(value));
	}
	
	public synchronized ByteBuffer encode(CharBuffer value) throws CharacterCodingException
	{
		return encoder.encode(value);
	}
	
	/** 
	 * Adds the provided line to the write buffer, encoding it as "US-ASCII" and 
	 * adding CRLF (standard network protocol line terminators).
	 **/
	 
	public synchronized void writeLine(String line) throws java.io.IOException
	{
		connection.log("SEND: " + line);
		write(CharBuffer.wrap(line + "\r\n"));
	}

	/** 
	 * Adds the provided String to the write buffer, encoding it as "US-ASCII". 
	 **/
	 
	public synchronized void write(String line) throws java.io.IOException
	{
		connection.log("SEND: \"" + line + "\"");
		write(CharBuffer.wrap(line));
	}
	
	/** 
	 * Adds the provided character buffer to the write buffer, encoding it as "US-ASCII". 
	 **/

	public synchronized int write(CharBuffer buffer) throws java.io.IOException
	{
		return write(encoder.encode(buffer));
	}
	
	/**
	 * Adds the provided ByteBuffer to the output buffer; no encoding is done.
	 **/
	 
	public synchronized int write(ByteBuffer buffer) throws java.io.IOException
	{
		int result;
		result = buffer.remaining();
		outputBuffer.put(buffer);
		return result;
	}
}
