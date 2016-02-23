package org.infodancer.service.api;

public interface Lifecycle 
{
	/** 
	 * Tries to start the object.  If the object cannot be started cleanly, a LifecycleException is thrown.  
	 * @throws LifecycleException
	 */
	public void start() throws LifecycleException;

	/** 
	 * Tries to stop the object.  If the object cannot be stopped cleanly, a LifecycleException is thrown.  
	 * @throws LifecycleException
	 */

	public void stop() throws LifecycleException;
}
