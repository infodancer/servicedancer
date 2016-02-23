/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service.api.async;

import java.io.IOException;

import org.infodancer.service.api.Service;
import org.infodancer.service.api.ServiceException;


/**
 **/

public interface AsyncService extends Service
{	
	/**
	 * Provides a ServiceHandler to process IO events on an incoming connection.
	 * In the event this method returns null, the connection will be dropped.
	 * @return a suitable ServiceHandler, or null if the connection should be dropped.
	 **/
	 
	public ServiceHandler service(AsyncConnection connection) throws ServiceException, IOException;
}
