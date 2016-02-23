package org.infodancer.service.netstring;

/**
 * A NetStringReceiver is intended to receive and process the data portion of a NetString.
 * There are two main purposes for an implementation of this class.  NetStrings are either 
 * buffered in memory for later processing or written to disk directly. 
 * @author mhunter
 *
 */
public interface NetStringReceiver 
{
	/**
	 * Retrieves the expected length of the NetString.
	 */
	public int getLength();
	
	/** 
	 * Retrieves the remaining length of the NetString.
	 */
	
	public int getRemaining();
	
	/**
	 * Does this NetStringReceiver need more bytes to complete?
	 * @return
	 */
	public boolean hasRemaining();
	
	/** 
	 * Reads from the provided ByteBuffer into the NetString.
	 * @param buffer
	 */
	public void receive(java.nio.ByteBuffer buffer) throws java.io.IOException;
	
	/**
	 * Reads from the provided ReadableByteChannel into the NetString.
	 * 
	 */
	
	public void receive(java.nio.channels.ReadableByteChannel channel) throws java.io.IOException;
	
	/**
	 * Indicates that the NetString has been fully received and resources can be discarded.
	 */

	public void close();
}
