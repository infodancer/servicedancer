package org.infodancer.msgdancer;

import java.io.OutputStream;
import java.util.List;

import org.infodancer.message.DeliveryException;
import org.infodancer.message.EmailAddress;
import org.infodancer.message.Mailbox;
import org.infodancer.service.api.EngineConfig;
import org.infodancer.service.api.ServiceException;
/**
 * A basic implementation of a MessageEngine intended for use in Junit tests.
 * @author matthew
 *
 */
public class TestMessageEngine implements MessageEngine
{

	@Override
	public void init(EngineConfig config) throws ServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws ServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws ServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLocalAddress(EmailAddress address)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean verifyEmailAddress(EmailAddress address)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String hash(EmailAddress address)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String store(byte[] data)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] retrieve(String msgid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(String msgid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enqueue(String msgid, EmailAddress recipient) throws MessageEngineException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getMessages(EmailAddress recipient) throws MessageEngineException {
		// TODO Auto-generated method stub
		return null;
	}
}
