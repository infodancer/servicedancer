package org.infodancer.service.api;

/** 
 * An Engine is a processor that provides "services for services", such as 
 * a common, in-process mail store for smtp, pop3, and imap services.  Engines
 * can be configured in the services.xml file and will be started before any
 * any ServiceListeners are created.  
 **/

public interface ServiceEngine
{
	/**
	 * Initializes the Engine, but does not start it running.
	 * @param config
	 * @throws ServiceException
	 */
	public void init(EngineConfig config) throws ServiceException;
	
	/**
	 * Starts the Engine, creating any threads necessary for the engine's operation.
	 * @throws ServiceException
	 */
	public void start() throws ServiceException;
	
	/**
	 * Signals the ServiceEngine to shut down.
	 * @throws ServiceException
	 */
	public void stop() throws ServiceException;
}
