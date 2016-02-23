package org.infodancer.service;

import java.util.concurrent.Callable;

import javax.naming.InitialContext;

import org.infodancer.engine.AbstractServiceEngine;

/**
 * Starts an Engine in its own thread.
 * @author matthew
 *
 */
public class StartEngineTask implements Callable<StartEngineTask>
{
	String name;
	AbstractServiceEngine engine;
	
	StartEngineTask(String name, AbstractServiceEngine engine)
	{
		this.name = name;
		this.engine = engine;
	}
	
	public StartEngineTask call() throws Exception
	{
		InitialContext context = null;
		
		try
		{
			context = new javax.naming.InitialContext();
			engine.start();
			context.bind(name, engine);
			return this;
		}
		
		catch (Exception e)
		{
			StringBuffer error = new StringBuffer();
			error.append("Exception while starting engine " + name);
			error.append(" handled by class " + engine.getClass().getName());
			System.err.println(error);
			e.printStackTrace(System.err);
			engine.stop();
			return this;
		}
		
		finally
		{
			try { context.close(); } catch (Exception e) { } 
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public AbstractServiceEngine getEngine()
	{
		return engine;
	}

	public void setEngine(AbstractServiceEngine engine)
	{
		this.engine = engine;
	}
}
