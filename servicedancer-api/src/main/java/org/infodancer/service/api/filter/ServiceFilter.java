/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service.api.filter;

import java.net.*;

/** 
 * Allows for basic filtering of connection attempts.  The default state is to 
 * allow a connection; if the sum of the filter results is negative, the connection
 * will be rejected.
 **/

public interface ServiceFilter
{
	public int permit(InetAddress socket);
}
