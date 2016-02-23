/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service.api.async;

import org.infodancer.service.api.Service;

/** 
 * A ServiceHandler maintains protocol state for a non-blocking Service, and is called
 * to process IO events related to that service.
 **/
 
public interface ServiceHandler
{
	/**
	 * Provides the set of IO operations (none, read, or write) that this handler is interested 
	 * in receiving. 
	 * @return int as described in java.nio.
	 */
	public int interestOps();
	
	/**
	 * Indicates whether the connection associated with this handler is still in a valid state (open).
	 * @return boolean true or false.
	 */
	public boolean isValid();
	
	public Service getService();
	
	/**
	 * Initializes the ServiceHandler in preperation for speaking the network protocol. 
	 * At a minimum, this method should be overridden to specify the interest operations the 
	 * ServiceHandler wants to receive; the interestOps() will be reset to 0 by the container
	 * before calling this method.
	 * @throws java.io.IOException
	 */
	public void start() throws java.io.IOException;
		
	/** 
	 * Does work on the service in response to an IO readiness event, returning a ServiceHandler to handle future
	 * events on this connection.  The ServiceManager will use an IO thread to call read() or write() depending on
	 * what IO events have been received.  After each such call, a separate thread will call service().  
	 * @return the ServiceHandler to use for future requests, or null if the connection should be dropped.
	 **/
	 
	public ServiceHandler service() throws java.io.IOException;
	
	/** 
	 * Terminates the connection as quickly and cleanly as possible within the
	 * protocol.
	 **/
	 
	public void stop() throws java.io.IOException;
	
	/** 
	 * Closes any resources currently open immediately, without any protocol 
	 * considerations.
	 **/
	 
	public void close() throws java.io.IOException;
}
