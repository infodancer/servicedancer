package org.infodancer.msgdancer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.infodancer.message.EmailAddress;
import org.infodancer.service.api.EngineConfig;
import org.infodancer.service.api.ServiceException;
import org.infodancer.service.api.domain.Domain;
import org.infodancer.service.api.domain.DomainManager;
import org.infodancer.user.User;
import org.infodancer.user.UserManager;

public abstract class AbstractMessageEngine implements MessageEngine
{
	protected DomainManager dm;
	
	/**
	 * Intended for test purposes with subclasses.
	 * @param dm
	 */
	public AbstractMessageEngine(DomainManager dm)
	{
		this.dm = dm;
	}
	
	public AbstractMessageEngine()
	{
		
	}

	public String calculateMessageId(byte[] data)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance(DEFAULT_DIGEST_ALGORITHM);
			md.update(data);
			return convert(md.digest());
		}
		
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			throw new RuntimeException(DEFAULT_DIGEST_ALGORITHM + " is not available in this JVM.");
		}
	}
	
	/** 
	 * Hashes an email address for use in a MessageEnvelope.
	 */
	public String hash(EmailAddress address) 
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance(DEFAULT_DIGEST_ALGORITHM);
			md.update(address.toString().getBytes());
			return convert(md.digest());
		}
		
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			throw new RuntimeException(DEFAULT_DIGEST_ALGORITHM + " is not available in this JVM.");
		}
	}

	private static char convertDigit(int value) 
	{
		value &= 0x0f;
		if (value >= 10)
		{
		    return ((char) (value - 10 + 'a'));
		}
		else
		{
			return ((char) (value + '0'));
		}
	 }

	public static String convert(byte bytes[]) 
	{
		StringBuffer sb = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) 
		{
		    sb.append(convertDigit((int) (bytes[i] >> 4)));
		    sb.append(convertDigit((int) (bytes[i] & 0x0f)));
		}
		return (sb.toString());
	}
	
	public boolean isLocalAddress(EmailAddress address)
	{
		String domainName = address.getDomain();
		Domain domain = dm.getDomain(domainName);
		if (domain != null) return true;
		else return false;
	}
	
	public boolean verifyEmailAddress(EmailAddress address)
	{
		String domainName = address.getDomain();
		Domain domain = dm.getDomain(domainName);
		if (domain != null)
		{
			String userName = address.getUser();
			UserManager um = domain.getUserManager();
			if (um != null)
			{
				User user = um.findUserByUsername(userName);
				if (user != null) return true;
				else return false;
			}
			else return false;
		}
		else return false;
	}

	@Override
	public abstract void init(EngineConfig config) throws ServiceException;

	@Override
	public abstract void start() throws ServiceException;
	@Override
	public abstract void stop() throws ServiceException;

	@Override
	public abstract String store(byte[] data) throws MessageEngineException;

	@Override
	public abstract byte[] retrieve(String msgid) throws MessageEngineException;

	@Override
	public abstract void enqueue(String msgid, EmailAddress recipient) throws MessageEngineException;
	
	public abstract List<String> getMessages(EmailAddress recipient) throws MessageEngineException;

	@Override
	public abstract boolean exists(String msgid) throws MessageEngineException;
}
