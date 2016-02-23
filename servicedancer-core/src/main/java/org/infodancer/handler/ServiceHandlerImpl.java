/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.handler;

import org.infodancer.service.ServiceManager;
import org.infodancer.service.api.async.AsyncService;

/** 
 * A ServiceHandler maintains protocol state for a non-blocking Service, and is called
 * to process IO events related to that service.
 **/
 
public interface ServiceHandlerImpl
{
	public AsyncService getService();
	public ServiceManager getServiceManager();
	public void setServiceManager(ServiceManager service);
	
	/**
	 * Indicates whether the connection associated with this handler is still in a valid state (open).
	 * @return boolean true or false.
	 */
	public boolean isValid();
	
	/**
	 * Provides the set of IO operations (none, read, or write) that this handler is interested 
	 * in receiving. 
	 * @return int as described in java.nio.
	 */
	public int interestOps();

	/** 
	 * Does work on the service in response to an IO readiness event.  
	 **/
	 
	public void service() throws java.io.IOException;
	
	/**
	 * Initializes the ServiceHandler in preparation for speaking the network protocol. 
	 * At a minimum, this method should be overridden to specify the interest operations the 
	 * ServiceHandler wants to receive; the interestOps() will be reset to 0 by the container
	 * before calling this method.
	 * @throws java.io.IOException
	 */
	public void start() throws java.io.IOException;
		
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
