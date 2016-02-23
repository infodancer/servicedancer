/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLEngine;

import org.infodancer.handler.AbstractServiceHandler;
import org.infodancer.handler.ServiceHandlerImpl;
import org.infodancer.service.api.async.AsyncService;
import org.infodancer.service.async.AsyncConnectionImpl;

/** 
 * Provides basic utility functions for a ServiceHandler.  
 **/
 
public abstract class AbstractSocketHandler extends AbstractServiceHandler implements ServiceHandlerImpl
{
	private static final int BUFFERSIZE = 32768;
	protected Charset charset = Charset.forName("UTF-8");
	protected CharsetDecoder decoder = charset.newDecoder();
	protected CharsetEncoder encoder = charset.newEncoder();
	protected ByteBuffer inputBuffer;
	protected ByteBuffer outputBuffer;
	protected Deque<String> lines;
	protected AsyncConnectionImpl connection;
	protected SSLEngine sslEngine;
	protected boolean blocked;
	
	public AbstractSocketHandler(AsyncService service, AsyncConnectionImpl connection)
	{
		super(service);
		this.closed = false;
		this.blocked = false;
		this.connection = connection;
		this.charset = Charset.forName("UTF-8");
		this.decoder = charset.newDecoder();
		this.decoder.onMalformedInput(CodingErrorAction.IGNORE);
		this.encoder = charset.newEncoder();
		this.encoder.onMalformedInput(CodingErrorAction.IGNORE);
		this.sslEngine = connection.getSSLEngine();
		this.inputBuffer = ByteBuffer.allocate(BUFFERSIZE);
		this.outputBuffer = ByteBuffer.allocate(BUFFERSIZE);
		this.lines = new ArrayDeque<String>();
		inputBuffer.clear();
		outputBuffer.clear();
	}	
	
	public int interestOps()
	{
		int result = 0;
		synchronized (outputBuffer)
		{
			if (outputBuffer.hasRemaining()) 
			{
				result = result | SelectionKey.OP_WRITE;
			}
		}
		
		synchronized (inputBuffer)
		{
			if (inputBuffer.capacity() > 0)
			{
				result = result | SelectionKey.OP_READ;
			}
		}
		 
		return result;	
	}
	
	/** 
	 * Reads data from the socketchannel and calls process() for each complete line available.
	 * Rather than blocking, each call will add the data to a buffer and return immediately 
	 * if a complete line is not available.  Consistent with BufferedReader.readLine(), a line 
	 * is considered to terminate with a '\n' and the '\r' is stripped.  This method has to use
	 * some evil hackery (eg, reading the ByteBuffer directly, in order to stop at a newline, rather 
	 * than read past it), but it is kept to a minimum.  There simply is no clean
	 * way to do this without implementing a whole new CharsetDecoder.
	 **/
	 
	public int readLine() throws java.io.IOException
	{
		synchronized (inputBuffer)
		{
			int result = -1;
			// Read the available data into the input buffer
			connection.log("Pre-read: " + inputBuffer);
			result = connection.read(inputBuffer);
			connection.log("Post-read: " + inputBuffer);
			inputBuffer.flip();

			// Scan for newlines and decode line by line
			int l = -1, b = -1;
			int limit = inputBuffer.limit();
			int position = inputBuffer.position();
			for (int count = position; count < limit; count++)
			{
				b = (int) inputBuffer.get(count);			
				if (b == 10)
				{
					// We have received a complete line
					ByteBuffer temp = inputBuffer.duplicate();
					temp.position(position);
					
					// If the last character was a CR, hide it; otherwise just hide the LF
					if (l == 13) temp.limit(count - 1);
					else temp.limit(count);
					
					String line = null;

					// The decoder doesn't like odd-number bytes 
					int length = temp.remaining();
					if (length != 1) 
					{
						int dlength = length;
						int dremain = temp.remaining();
						int dposition = temp.position();
						int dlimit = temp.limit();
						try
						{							
							// Add the line to a buffer for processing in service()
							synchronized (decoder)
							{
								 line = decoder.decode(temp).toString();
							}
						}
						
						catch (Exception e)
						{
							e.printStackTrace();
							System.out.println("dlength: " + dlength);
							System.out.println("dremain: " + dremain);
							System.out.println("dposition: " + dposition);
							System.out.println("dlimit: " + dlimit);
							line = "";
						}
					}
					else 
					{
						byte[] single = new byte[1];
						single[0] = temp.get();
						line = new String(single);
					}
					receivedLine(line);
					
					// Whatever preprocessing needs to be done, we do it here.
					preprocess(line);
					
					// Position the buffer to start at the beginning of the next line
					position = count + 1;
				}
				l = b;
			}
			inputBuffer.position(position);
			inputBuffer.compact();
			return result;
		}
	}

	protected void preprocess(String line) 
	{
		synchronized (lines)
		{
			lines.addLast(line);
		}
	}

	public ByteBuffer encode(String value) throws CharacterCodingException
	{
		return encode(CharBuffer.wrap(value));
	}
	
	public ByteBuffer encode(CharBuffer value) throws CharacterCodingException
	{
		synchronized (encoder)
		{
			return encoder.encode(value);
		}
	}
	
	/** 
	 * Adds the provided line to the write buffer, encoding it as "US-ASCII" and 
	 * adding CRLF (standard network protocol line terminators).
	 **/
	 
	public void writeLine(String line) throws java.io.IOException
	{
		sentLine(line);
		write(CharBuffer.wrap(line + "\r\n"));
	}

	private synchronized void sentLine(String line) 
	{
		try 
		{
			if (tracelog != null)
			{
				if (tracelogWriter == null)
				{
					tracelogWriter = new BufferedWriter(new FileWriter(tracelog));
				}
				
				tracelogWriter.write('<');
				tracelogWriter.write(line);
				tracelogWriter.newLine();
				tracelogWriter.flush();
			}
		}
		
		catch (Exception e) 
		{ 
			e.printStackTrace();
		}
	}

	private synchronized void receivedLine(String line)
	{
		try
		{
			if ((tracelog != null) && (line != null))
			{
				if (tracelogWriter == null)
				{
					tracelogWriter = new BufferedWriter(new FileWriter(tracelog));
				}
				
				tracelogWriter.write('>');
				tracelogWriter.write(line);
				tracelogWriter.newLine();
				tracelogWriter.flush();
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/** 
	 * Adds the provided String to the write buffer, encoding it as "US-ASCII". 
	 **/
	 
	public void write(String line) throws java.io.IOException
	{
		connection.log("SEND: \"" + line + "\"");
		write(CharBuffer.wrap(line));
	}
	
	/** 
	 * Adds the provided character buffer to the write buffer, encoding it as "US-ASCII". 
	 **/

	public void write(CharBuffer buffer) throws java.io.IOException
	{
		synchronized (encoder)
		{
			write(encoder.encode(buffer));
		}
	}
	
	/**
	 * Adds the provided ByteBuffer to the output buffer; no encoding is done.
	 * This method will try not to block, but may block if not enough space is available
	 * in the output buffer.  
	 **/
	 
	public synchronized void write(ByteBuffer buffer) throws java.io.IOException
	{
		synchronized (outputBuffer)
		{
			do
			{
				try
				{
					outputBuffer.put(buffer);
				}
				
				catch (BufferOverflowException e)
				{
					blocked = true;
					connection.log("Buffer overflow (capacity: " + outputBuffer.capacity() + " remaining: " + outputBuffer.remaining() + " required: " + buffer.remaining());
					try 
					{ 
						outputBuffer.wait(6000000);
						blocked = false;
					} 
					
					catch (InterruptedException ee) 
					{ 
						throw e;
					}
				}
			}
			while (buffer.remaining() > 0);
		}
	}
	
	/**
	 * Writes available data to the outgoing socket, if any.
	 */
	public void write() throws java.io.IOException
	{
		synchronized (outputBuffer)
		{
			if ((!closed) && (connection.isOpen()))
			{
				connection.log("EVENT: SelectionKey.OP_WRITE");
				outputBuffer.flip();
				if (outputBuffer.hasRemaining())
				{
					int result = connection.write(outputBuffer);
					if (result == -1) 
					{
						connection.close();
						throw new java.io.IOException("IO Error writing to ServletChannel!");
					}
					else 
					{
						connection.log("EVENT: Wrote " + result + " bytes.");
					}
				}	
						
				outputBuffer.compact();
				outputBuffer.notifyAll();
				if (!connection.isOpen()) close();
			}
		}
	}

	/**
	 * Reads available data from the incoming socket, if any.
	 */
	
	public void read() throws java.io.IOException
	{
		synchronized (inputBuffer)
		{
			if ((!closed) && (connection.isOpen()))
			{
				connection.log("EVENT: SelectionKey.OP_READ");
				int result = readLine();
				if (result == -1) close();
				else connection.log("EVENT: Read " + result + " bytes.");
				if (!connection.isOpen()) close();
			}
		}
	}
	
	/** 
	 * The default implementation of this method reads lines from the client and 
	 * passes each line to the process() method.  Writes should be performed using 
	 * writeLine(), and will be buffered until the socket reports
	 * itself as ready for writing.
	 **/

	public synchronized void service() throws java.io.IOException
	{
		read();
		synchronized (lines)
		{
			String line = null;
			while ((line = lines.pollFirst()) != null)
			{
				process(line);
			}
		}
		write();
		if (!connection.isOpen()) close();
	}
	
	public boolean isValid()
	{
		if (closed) return false;
		else return connection.isOpen(); 
	}
	
	/** 
	 * This function will be called once for every line read.  It is a utility
	 * function designed to assist in writing handlers for line-oriented protocols.
	 * The default implementation of service() calls this method when a complete
	 * line is received; subclasses may call readLine() by hand or leave the default
	 * service() implementation in place if line-oriented data is sufficient.  
	 **/
	 
	public abstract void process(String line) throws java.io.IOException;
	
	/**
	 * Writes all data in the output buffer before returning.
	 * @throws java.io.IOException
	 */
	
	public synchronized void flush() throws java.io.IOException
	{
		synchronized (outputBuffer)
		{
			if ((!closed) && (connection.isOpen()))
			{
				connection.log("EVENT: SelectionKey.OP_WRITE");
				outputBuffer.flip();
				while (outputBuffer.hasRemaining())
				{
					int result = connection.write(outputBuffer);
					if (result == -1) 
					{
						connection.close();
						throw new java.io.IOException("IO Error writing to ServletChannel!");
					}
					else 
					{
						connection.log("EVENT: Wrote " + result + " bytes.");
					}
				}			
				outputBuffer.compact();
				outputBuffer.notifyAll();
				if (!connection.isOpen()) close();
			}
		}
	}
	
	/** 
	 * Closes the connection, and tries to finish writing any data in the output 
	 * buffer before closing; this method will block until all pending writes
	 * are complete.
	 **/
	 
	public synchronized void close() throws java.io.IOException
	{
		try { if (connection.isConnected()) flush(); } catch (Exception e) { }  
		try { connection.close(); } catch (Exception e) { } 
		try { super.close(); } catch (Exception e) { }
		if (tracelogWriter != null) try { tracelogWriter.close(); } catch (Exception e) { }
	}	
	
	public boolean isBlocked()
	{
		return blocked;
	}
	
	public class LineWorker implements Callable<Integer> 
	{	
		public Integer call() throws Exception
		{
			int result = 0;
			if (!closed)
			{
				synchronized (lines)
				{
					String line = null;
					while ((line = lines.pollFirst()) != null)
					{
						process(line);
						result++;
					}
				}
				if (!connection.isOpen()) close();
			}			
			return result;
		}
	}
}
