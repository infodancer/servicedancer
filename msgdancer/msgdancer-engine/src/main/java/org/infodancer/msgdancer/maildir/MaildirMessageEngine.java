package org.infodancer.msgdancer.maildir;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.infodancer.message.EmailAddress;
import org.infodancer.message.maildir.MaildirFolder;
import org.infodancer.msgdancer.AbstractMessageEngine;
import org.infodancer.msgdancer.MessageEngineException;
import org.infodancer.service.api.EngineConfig;
import org.infodancer.service.api.ServiceException;

public class MaildirMessageEngine extends AbstractMessageEngine 
{
	private File queuedir;
	private File msgdir;
	private File envdir;
	private File tmpdir;
	
	@Override
	public void init(EngineConfig config) throws ServiceException 
	{
	
	}

	@Override
	public void start() throws ServiceException
	{
		if (!queuedir.exists()) queuedir.mkdirs();
		if (queuedir.exists()) 
		{
			if (queuedir.canRead() && queuedir.canWrite())
			{
				if (!msgdir.exists()) msgdir.mkdirs();
				if (!msgdir.canRead() || !msgdir.canWrite()) throw new ServiceException(msgdir.getAbsolutePath() + " is read or write restricted!");
				if (!envdir.exists()) envdir.mkdirs();
				if (!envdir.canRead() || !envdir.canWrite()) throw new ServiceException(envdir.getAbsolutePath() + " is read or write restricted!");
				if (!tmpdir.exists()) tmpdir.mkdirs();
				if (!tmpdir.canRead() || !tmpdir.canWrite()) throw new ServiceException(tmpdir.getAbsolutePath() + " is read or write restricted!");
			}
			else throw new ServiceException(queuedir.getAbsolutePath() + " is read or write restricted!");
		}
		else throw new ServiceException(queuedir.getAbsolutePath() + " does not exist!");
	}

	@Override
	public void stop() throws ServiceException 
	{
	
	}
	
	public String getQueueDirectory() 
	{
		return queuedir.getAbsolutePath();
	}

	public void setQueueDirectory(String queuedir) 
	{
		this.queuedir = new File(queuedir);
		this.msgdir = new File(this.queuedir.getAbsolutePath() + File.separator + "msg");
		this.envdir = new File(this.queuedir.getAbsolutePath() + File.separator + "env");
		this.tmpdir = new File(this.queuedir.getAbsolutePath() + File.separator + "tmp");
	}

	/**
	 * Stores a message as a byte[] and returns a msgid.
	 * @return msgid in String format.
	 */
	@Override
	public String store(byte[] data) throws MessageEngineException 
	{
		BufferedOutputStream output = null;
		
		try
		{
			File tmpfile = createTempFile();
			output = new BufferedOutputStream(new FileOutputStream(tmpfile));
			output.write(data);
			File msgfile = createMessageFile(tmpfile.getName());
			tmpfile.renameTo(msgfile);
			return msgfile.getName();
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			throw new MessageEngineException(e);
		}
		
		finally
		{
			try { if (output != null) output.close(); } catch (Exception e) { e.printStackTrace();}
		}
	}
	
	/**
	 * Retrieves a message based on the msgid.
	 */
	@Override
	public byte[] retrieve(String msgid) throws MessageEngineException
	{
		BufferedInputStream input = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		try
		{
			File msgfile = createMessageFile(msgid);
			input = new BufferedInputStream(new FileInputStream(msgfile));
			int i = 0;
			while ((i = input.read()) != -1) output.write(i);
			return output.toByteArray();
		}

		catch (Exception e)
		{
			e.printStackTrace();
			throw new MessageEngineException(e);
		}
		
		finally
		{
			try { if (input != null) input.close(); } catch (Exception e) { e.printStackTrace();}
			try { if (output != null) output.close(); } catch (Exception e) { e.printStackTrace();}
		}
	}

	@Override
	public void enqueue(String msgid, EmailAddress recipient) throws MessageEngineException
	{
		BufferedOutputStream output = null;
		
		try
		{
			if (exists(msgid))
			{
				if (isLocalAddress(recipient))
				{
					File maildirFile = extractMaildirLocation(recipient);
					MaildirFolder maildir = new MaildirFolder(maildirFile);
				}
				else
				{
					// Queue for remote delivery
					MaildirMessageEnvelope env = new MaildirMessageEnvelope(msgid, recipient);
					File tmpfile = createTempFile();
					MaildirMessageEnvelope.exportFile(env, tmpfile);
					File envfile = createEnvelopeFile(tmpfile.getName());
					tmpfile.renameTo(envfile);
				}
			}
			else throw new FileNotFoundException(msgid);
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			throw new MessageEngineException(e);
		}
		
		finally
		{
			try { if (output != null) output.close(); } catch (Exception e) { e.printStackTrace();}
		}
	}
	
	private File extractMaildirLocation(EmailAddress recipient) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/** 
	 * Creates a temporary file into which a message can be delivered, using the 
	 * JVM's createTempFile() method.  This does not exactly conform to the maildir
	 * specification, but it appears to be as close as can be achieved without 
	 * native code.
	 **/
	 
	protected File createTempFile() throws IOException, java.net.UnknownHostException
	{
		String prefix = System.currentTimeMillis() + ".";
		String suffix = "." + java.net.InetAddress.getLocalHost().getHostName();
		return File.createTempFile(prefix, suffix, tmpdir);		
	}
	
	/** 
	 * Creates a message filename from a temporary filename.  Note that the actual 
	 * file is not created; the temporary file should be renamed to the resulting
	 * filename when delivery is complete.
	 **/
	protected File createMessageFile(String name) throws IOException
	{
		return new File(msgdir + File.separator + name);		
	}

	/** 
	 * Creates a message filename from a temporary filename.  Note that the actual 
	 * file is not created; the temporary file should be renamed to the resulting
	 * filename when delivery is complete.
	 **/
	protected File createEnvelopeFile(String name) throws IOException
	{
		return new File(envdir + File.separator + name);		
	}

	
	
	@Override
	public boolean exists(String msgid) throws MessageEngineException
	{
		try
		{
			File msgfile = createMessageFile(msgid);
			if (msgfile.exists()) return true;
			else return false;
		}
		
		catch (Exception e)
		{
			throw new MessageEngineException(e);
		}
	}

	@Override
	public List<String> getMessages(EmailAddress recipient) throws MessageEngineException
	{
		return null;
	}
}
