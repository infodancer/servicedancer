package org.infodancer.service.api.sync;

import java.io.IOException;

import org.infodancer.service.api.Service;
import org.infodancer.service.api.ServiceException;

public interface SyncService extends Service
{
	/**
	 * Services an incoming connection request.  
	 * Provides a ServiceHandler to process IO events on an incoming connection.
	 **/
	 
	public void service(SyncConnection connection) throws IOException, ServiceException;

}
