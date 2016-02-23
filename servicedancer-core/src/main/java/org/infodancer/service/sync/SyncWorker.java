package org.infodancer.service.sync;

import org.infodancer.service.api.sync.SyncConnection;
import org.infodancer.service.api.sync.SyncService;

public class SyncWorker implements java.util.concurrent.Callable<SyncConnection>
{
	SyncService service;
	SyncListener listener;
	SyncConnection connection;
	
	public SyncWorker(SyncListener listener, SyncService service, SyncConnection connection)
	{
		this.service = service;
		this.listener = listener;
		this.connection = connection;
	}
	
	public SyncConnection call() throws Exception
	{
		service.service(connection);
		return connection;
	}
}
