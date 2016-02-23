/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.nio.channels.*;

import org.infodancer.handler.ServiceHandlerImpl;

/** 
 * Provides basic utility functions for a ServiceHandler.
 **/
 
public abstract class AbstractPacketHandler implements ServiceHandlerImpl
{

	public abstract void readPacket(DatagramChannel channel);
	
	public abstract void writePacket(DatagramChannel channel);

	/** 
	 * The default implementation of this method reads lines from the client and 
	 * passes each line to a subclass through the process() method.  
	 **/

	public synchronized void service(SelectionKey key) throws java.io.IOException
	{
		DatagramChannel packetchannel = (DatagramChannel) key.channel();

		if (key.isReadable())
		{
			readPacket(packetchannel);
		}
		
		if (key.isWritable()) 
		{
			writePacket(packetchannel);
		}
	}
}
