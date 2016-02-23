package org.infodancer.service.api.domain;

import org.infodancer.service.api.Lifecycle;

public interface DomainManager extends Lifecycle
{
	public static final String CONTEXT_DOMAIN_MANAGER = "domainmanager";
	public Domain getDomain(String name);
	public void createDomain(String name);
	public void removeDomain(String name);
}
