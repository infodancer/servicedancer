/**
 * Copyright by matthew on Feb 3, 2006 as part of the infodancer services project.
 */
package org.infodancer.service.api;

/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

/**
 * A ServiceComponent is a class designed run beneath the umbrella of a ServiceManager, configured according to a subtree of the services.xml file.
 * ServiceComponents can be started and stopped by the manager and are intended primarily as resources that can be obtained from within a service.
 * @author matthew
 *
 */
public interface ServiceComponent extends Lifecycle
{
	/**
	 * Sets the name of this component.
	 * @param name
	 */
	
	public void setName(String name);
	
	/**
	 * Retrieves the name of this component.
	 * @return
	 */
	public String getName();		
}
