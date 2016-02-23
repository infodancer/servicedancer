package org.infodancer.msgdancer;

import junit.framework.TestCase;

import org.infodancer.message.EmailAddress;
import org.infodancer.message.InvalidAddressException;
import org.infodancer.service.api.domain.Domain;
import org.infodancer.service.api.domain.TestDomainManager;
import org.infodancer.user.UserManager;

public abstract class AbstractMessageEngineTest extends TestCase
{
	protected MessageEngine msgengine;
	protected TestDomainManager dm;
	
	public void setUp() throws Exception
	{
		dm = new TestDomainManager();
		dm.createDomain("example.com");
		Domain domain = dm.getDomain("example.com");
		UserManager um = domain.getUserManager();
		um.createUser("username", "password");
	}
	
	public void testStore() throws MessageEngineException
	{
    	StringBuilder s = new StringBuilder();
    	s.append("From: username@example.com\r\n");
    	s.append("To: username@example.");
    	byte[] data1 = s.toString().getBytes();
    	String msgid = msgengine.store(data1);
    	assertNotNull(msgid);
	}

	public void testRetrieve() throws MessageEngineException
	{
    	StringBuilder s = new StringBuilder();
    	s.append("From: username@example.com\r\n");
    	s.append("To: username@example.");
    	byte[] data1 = s.toString().getBytes();
    	String msgid = msgengine.store(data1);
    	assertNotNull(msgid);
    	byte[] data2 = msgengine.retrieve(msgid);
    	assertNotNull(data1);
    	assertNotNull(data2);
    	assertEquals(data1.length, data2.length);
    	for (int i = 0; i < data1.length; i++)
    	{
    		assertTrue(data1[i] == data2[i]); 
    	}
	}
	
	public void testEnqueue()
	{
		
	}
	
	public void testDequeue()
	{
		
	}
}
