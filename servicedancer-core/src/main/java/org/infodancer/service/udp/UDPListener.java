package org.infodancer.service.udp;

import java.io.File;

import org.infodancer.service.ServiceClassLoader;
import org.infodancer.service.ServiceListener;
import org.infodancer.service.ServiceManager;
import org.infodancer.service.api.ServiceException;
import org.infodancer.service.api.udp.UDPService;

public class UDPListener extends ServiceListener
{
	UDPService service;

	public UDPListener(ClassLoader loader, File directory)
	{
		super(loader, directory);
	}

	public void run()
	{
		
	}

	@Override
	public void start() throws ServiceException
	{
		
	}

	@Override
	public void stop() throws ServiceException
	{
		
	}
	
	public void setUDPService(UDPService service)
	{
		this.service = service;
	}
}
