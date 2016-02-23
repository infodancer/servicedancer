/**
 * Copyright by matthew on Feb 3, 2006 as part of the infodancer services project.
 */
package org.infodancer.service;

import org.infodancer.service.api.ServiceComponent;

/**
 * @author matthew
 *
 */
public abstract class AbstractComponent implements ServiceComponent
{
	protected String name;
	protected ServiceManager manager;
	
	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setServiceManager(ServiceManager manager)
	{
		this.manager = manager;
	}

	public ServiceManager getServiceManager()
	{
		return manager;
	}

}
