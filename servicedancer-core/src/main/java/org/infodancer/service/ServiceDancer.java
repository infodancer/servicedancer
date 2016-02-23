package org.infodancer.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.spi.NamingManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.infodancer.archive.JarArchive;
import org.infodancer.archive.JarFileFilter;
import org.infodancer.context.ContextObjectFactoryBuilder;
import org.infodancer.context.InitialContextFactory;
import org.infodancer.context.InitialContextFactoryBuilder;
import org.infodancer.domain.DomainClassLoader;
import org.infodancer.domain.entity.EntityDomainManager;
import org.infodancer.engine.EngineClassLoader;
import org.infodancer.service.api.ServiceEngine;
import org.infodancer.service.api.ServiceException;
import org.infodancer.service.api.async.AsyncService;
import org.infodancer.service.api.domain.Domain;
import org.infodancer.service.api.domain.DomainManager;
import org.infodancer.service.api.filter.ServiceFilter;
import org.infodancer.service.api.sync.SyncService;
import org.infodancer.service.api.udp.UDPService;
import org.infodancer.service.async.AsyncListener;
import org.infodancer.service.sync.SyncListener;
import org.infodancer.service.udp.UDPListener;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * ServiceDancer is the entry point (following the classpath machinations in 
 * bootstrap.jar) for the ServiceDancer network application server.
 * @author matthew
 *
 */
public class ServiceDancer implements Runnable
{
	private static final String DIRECTORY = "directory";
	private static final String FILTER = "Filter";
	private static final String SYNC_LISTENER = "SyncListener";
	private static final String ASYNC_LISTENER = "AsyncListener";
	private static final String UDP_LISTENER = "UDPListener";
	private static final String CONTEXT = "context";
	private static final String SERVICE = "service";
	private static final String SERVICES = "services";
	private static final String ENGINE = "engine";
	private static final String ENGINES = "engines";
	private static final String DOMAIN = "domain";
	private static final String DOMAINS = "domains";
	private static final String SERVICE_MANAGER = "ServiceManager";
	private ClassLoader loader;
	private File configFile;
	
	public ServiceDancer()
	{
		
	}
	
	public void run()
	{
		try
		{
			// Identify the configuration file
			configFile = new File("/opt/servicedancer/servicedancer.xml");
			
			// Acquire the right classloader, and extend it
			loader = (URLClassLoader) Thread.currentThread().getContextClassLoader(); 
			
			// Create a ServiceManager instance
			ServiceManager manager = parseConfiguration();

			// Initialize the initial context builder
			InitialContextFactoryBuilder contextFactory = new InitialContextFactoryBuilder(manager.getContextFile());
			NamingManager.setInitialContextFactoryBuilder(contextFactory);
			
			// Initialize the ObjectFactoryBuilder
			ContextObjectFactoryBuilder builder = new ContextObjectFactoryBuilder();
			NamingManager.setObjectFactoryBuilder(builder);

			// Start the manager
			manager.start();
		}
		
		catch (Exception e)
		{
			// log.severe("Unable to start ServiceManager!");
			e.printStackTrace();
		}
	}

	private File parseArgumentsForConfigFile(String[] args)
	{
		if (args.length >= 1) return new File(args[0]);
		else return new File("servicedancer.xml");
	}

	private ServiceManager parseConfiguration() throws ServiceException
	{
		try
		{
			ServiceManager manager = null;
			DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(configFile);
	
			NodeList children = document.getChildNodes();
			for (int count = 0; count < children.getLength(); count++)
			{
				Node child = children.item(count);
				String name = child.getNodeName();
				if (name.equalsIgnoreCase(SERVICE_MANAGER))
				{
					manager = parseServiceManagerNode(child);
				}
			}
			
			return manager;
		}

		catch (IOException e)
		{
			throw new ServiceException(e);
		}

		catch (SAXException e)
		{
			throw new ServiceException(e);
		}

		catch (ParserConfigurationException e)
		{
			throw new ServiceException(e);
		}
	}

	private File parseFileName(File directory, String name)
	{
		if (name.startsWith("/")) return new File(name);
		else return new File(directory + File.separator + name);
	}

	/**
	 * Creates a ClassLoader from the specified library path, and sets it to be the 
	 * current thread's class loader.
	 */
	public static ClassLoader createClassLoader(List<File> jars) throws Exception
	{
		java.util.ArrayList<java.net.URL> urls = new java.util.ArrayList<java.net.URL>();
		for (java.io.File jarFile : jars)
		{
			System.out.println("Adding " + jarFile + " to classloader.");
			urls.add(jarFile.toURI().toURL());
		}

		URLClassLoader loader = new URLClassLoader(urls.toArray(new java.net.URL[urls.size()]));
		return loader;
	}
	
	private ServiceManager parseServiceManagerNode(Node node)
	throws ServiceException
	{
		String name = node.getNodeName();
		if (name.equalsIgnoreCase(SERVICE_MANAGER))
		{
			ServiceManager manager = new ServiceManager();
			File homedir = new File(".");
			
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++)
			{
				Node attribute = attributes.item(i);
				String attributeName = attribute.getNodeName();
				String attributeValue = attribute.getNodeValue();
				
				if ("classpath".equalsIgnoreCase(attributeName))
				{
					// manager.setClasspath(attribute.getNodeValue());
				}
				else if ("home".equalsIgnoreCase(attributeName))
				{
					homedir = new File(attributeValue).getAbsoluteFile();
				}
			}
			
			NodeList children = node.getChildNodes();
			for (int count = 0; count < children.getLength(); count++)
			{
				Node child = children.item(count);
				String childname = child.getNodeName();
				String childvalue = child.getNodeValue();
				
				if (CONTEXT.equalsIgnoreCase(childname))
				{
					// Load the defined context file
					if (childvalue == null) childvalue = "context.xml";
					File contextFile = parseFileName(homedir, childvalue);
					manager.setContextFile(contextFile);
				}
				else if (ENGINES.equalsIgnoreCase(childname))
				{
					// Load the defined engines
					Map<String,ServiceEngine> engines = parseEngines(homedir, child);
					for (String engineName : engines.keySet())
					{
						manager.addEngine(engineName, engines.get(engineName));
					}
				}
				else if (SERVICES.equalsIgnoreCase(childname))
				{
					// Load the defined services
					Map<String,ServiceListener> listeners = parseServices(homedir, child);
					for (String serviceName : listeners.keySet())
					{
						manager.addServiceListener(serviceName, listeners.get(serviceName));
					}					
				}
				else if (DOMAINS.equalsIgnoreCase(childname))
				{
					DomainManager domainManager = parseDomains(homedir, child);
					
				}
			}
			
			return manager;
		}
		else throw new ServiceException("ServiceManager must be configured from a node named ServiceManager!");
	}
	
	private DomainManager parseDomains(File homedir, Node node)
	throws ServiceException
	{
		String name = node.getNodeName();
		if (name.equalsIgnoreCase(DOMAINS))
		{
			DomainManager result = null; 
			NamedNodeMap attributes = node.getAttributes();
			Node classNode = attributes.getNamedItem("class");
			if (classNode != null)
			{
				try
				{
					String clazz = classNode.getNodeValue();
					result = (DomainManager) loader.loadClass(clazz).newInstance();
					parseConfigurationNode(result, node);

					NodeList children = node.getChildNodes();
					for (int count = 0; count < children.getLength(); count++)
					{
						Node child = children.item(count);
						String childname = child.getNodeName();
						
						if (childname.equalsIgnoreCase(DOMAIN))
						{
							// TODO Not implemented yet -- name issues
						}
					}
				}
				
				catch (Exception e)
				{
					e.printStackTrace();
					throw new ServiceException(e);
				}
			}
			return result;
		}
		else throw new ServiceException("Domains must be configured from a node named Domains!");
	}

	private Map<String,ServiceListener> parseServices(File home, Node node)
	throws ServiceException
	{
		String name = node.getNodeName();
		if (name.equalsIgnoreCase(SERVICES))
		{
			Map<String,ServiceListener> listeners = new TreeMap<String,ServiceListener>(); 
			// Load the services directory
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++)
			{
				Node attribute = attributes.item(i);
				String attributeName = attribute.getNodeName();
				String attributeValue = attribute.getNodeValue();
				if (DIRECTORY.equalsIgnoreCase(attributeName))
				{
					listeners = loadListeners(new File(home + File.separator + attributeValue));
				}
			}
			return listeners;
		}
		else throw new ServiceException("Services must be configured from a node named Services!");
	}

	private Map<String,ServiceEngine> parseEngines(File home, Node node)
	throws ServiceException
	{
		String name = node.getNodeName();
		if (name.equalsIgnoreCase(ENGINES))
		{
			Map<String,ServiceEngine> engines = new TreeMap<String,ServiceEngine>(); 
			
			// Load the engine directory
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++)
			{
				Node attribute = attributes.item(i);
				String attributeName = attribute.getNodeName();
				String attributeValue = attribute.getNodeValue();
				if (DIRECTORY.equalsIgnoreCase(attributeName))
				{
					engines = loadEngines(new File(home + File.separator + attributeValue));
				}
			}
			return engines;
		}
		else throw new ServiceException("Engines must be configured from a node named Engines!");
	}
	
	private Map<String,ServiceEngine> loadEngines(File directory)
	{
		System.out.println("Scanning for engines in: " + directory.getAbsolutePath());
		Map<String,ServiceEngine> result = new TreeMap<String,ServiceEngine>();
		if (directory.isDirectory() && directory.exists())
		{
			extractJars(directory);
			for (File file : directory.listFiles())
			{
				if (file.isDirectory())
				{
					System.out.println("Configuring engine: " + file);
					ServiceEngine engine;
					
					try
					{
						File engineConfigFile = new File(file + File.separator + "engine.xml");
						if (engineConfigFile.exists())
						{
							engine = loadEngine(file, engineConfigFile);
							if (engine != null) result.put(file.getName(), engine);
						}
					}
					
					catch (Exception e)
					{
						System.out.println("Error loading engine from: " + file.getAbsolutePath());
						e.printStackTrace();
					}
				}
			}
		}
		return result;
	}

	private ServiceEngine loadEngine(File directory, File configFile) 
	throws SAXException, IOException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		if (directory.isDirectory() && (directory.exists()))
		{
			ServiceEngine engine = parseEngineConfigFile(configFile);
			
			return engine;
		}
		else throw new IllegalArgumentException("Engine directory " + directory.getAbsolutePath() + " does not exist or is not a directory!");
	}

	private ServiceEngine parseEngineConfigFile(File configFile) 
	throws SAXException, IOException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		ServiceEngine engine = null;
		DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(configFile);

		NodeList children = document.getChildNodes();
		for (int count = 0; count < children.getLength(); count++)
		{
			Node child = children.item(count);
			String name = child.getNodeName();
			if (ENGINE.equalsIgnoreCase(name))
			{
				String engineClassName = parseEngineNodeClass(child);
				engine = (ServiceEngine) loader.loadClass(engineClassName).newInstance();
			}
		}
		
		return engine;
	}

	private String parseEngineNodeClass(Node node)
	{
		String name = node.getNodeName();
		if (ENGINE.equalsIgnoreCase(name))
		{
			NamedNodeMap attributes = node.getAttributes();
			Node attribute = attributes.getNamedItem("class");
			if (attribute != null) return attribute.getNodeValue();
		}
		return null;
	}

	private void loadContext(File contextFile)
	{
		if ((contextFile.exists()) && (contextFile.canRead()))
		{
			InitialContextFactory.setContextFile(contextFile);
		}
	}

	private Map<String,ServiceListener> loadListeners(File serviceDirectory) throws ServiceException
	{
		System.out.println("Scanning for services in: " + serviceDirectory.getAbsolutePath());
		Map<String,ServiceListener> result = new TreeMap<String,ServiceListener>();
		if (serviceDirectory.exists() && (serviceDirectory.isDirectory()))
		{
			extractJars(serviceDirectory);
			File[] serviceFiles = serviceDirectory.listFiles();
			for (File serviceFile : serviceFiles)
			{
				if (serviceFile.isDirectory())
				{
					ServiceListener listener;
					System.out.println("Configuring service: " + serviceFile);
					
					File serviceConfigFile = new File(serviceFile + File.separator + "service.xml");
					if (serviceConfigFile.exists())
					{
						try
						{
							listener = parseListenerConfig(loader, serviceFile, serviceConfigFile);
							if (listener != null) result.put(serviceFile.getName(), listener);
						}
						
						catch (Throwable e)
						{					
							e.printStackTrace();
							String msg = "Could not start service from " + serviceFile + ";";
							msg += serviceConfigFile + " was not parsed successfully.";
							throw new ServiceException(msg);							
						}
					}
					else
					{
						String msg = "Could not start service from " + serviceFile + ";";
						msg += serviceConfigFile + " does not exist.";
						throw new ServiceException(msg);
					}
				}
			}
		}
		return result;
	}

	private ServiceListener parseListenerConfig(ClassLoader loader, File directory, File configFile) throws Exception
	{
		DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(configFile);
		
		ServiceListener listener = null;
		NodeList children = document.getChildNodes();
		for (int count = 0; count < children.getLength(); count++)
		{
			Node child = children.item(count);
			String name = child.getNodeName();
			if (name.equalsIgnoreCase(ASYNC_LISTENER))
			{
				listener = createAsyncListener(loader, directory, child);
			}
			else if (name.equalsIgnoreCase(SYNC_LISTENER))
			{
				listener = createSyncListener(loader, directory, child);
			}
			else if (name.equalsIgnoreCase(UDP_LISTENER))
			{
				listener = createUDPListener(loader, directory, child);
			}
		}
		
		return listener;
	}

	private ServiceListener createAsyncListener(ClassLoader loader, File directory, Node node) throws Exception
	{
		String nodename = node.getNodeName();
		if (nodename.equalsIgnoreCase(ASYNC_LISTENER))
		{
			AsyncListener listener = new AsyncListener(loader, directory);
			parseConfigurationNode(listener, node);
			
			NodeList children = node.getChildNodes();
			if (children != null)
			{
				for (int count = 0; count < children.getLength(); count++)
				{
					Node child = children.item(count);
					String childname = child.getNodeName();
					if (childname.equalsIgnoreCase(SERVICE))
					{
						AsyncService service = createAsyncService(loader, child);
						listener.setAsyncService(service);
					}
					else if (childname.equalsIgnoreCase(FILTER))
					{
						createServiceFilter(loader, child);
					}
				}
			}
			
			return listener;
		}
		else throw new ServiceException("Listener must be created by a node named listener!");
	}

	private SyncListener createSyncListener(ClassLoader loader, File directory, Node node) throws Exception
	{
		String nodename = node.getNodeName();
		if (nodename.equalsIgnoreCase(SYNC_LISTENER))
		{
			SyncListener listener = new SyncListener(loader, directory);
			parseConfigurationNode(listener, node);
			
			NodeList children = node.getChildNodes();
			if (children != null)
			{
				for (int count = 0; count < children.getLength(); count++)
				{
					Node child = children.item(count);
					String childname = child.getNodeName();
					if (childname.equalsIgnoreCase(SERVICE))
					{
						SyncService service = createSyncService(loader, child);
						listener.setSyncService(service);
					}
					else if (childname.equalsIgnoreCase(FILTER))
					{
						createServiceFilter(loader, child);
					}
				}
			}
			
			return listener;
		}
		else throw new ServiceException("Listener must be created by a node named listener!");
	}

	private ServiceListener createUDPListener(ClassLoader loader, File directory, Node node)
	throws ServiceException
	{
		String nodename = node.getNodeName();
		if (nodename.equalsIgnoreCase(UDP_LISTENER))
		{
			UDPListener listener = new UDPListener(loader, directory);
			parseConfigurationNode(listener, node);
			
			NodeList children = node.getChildNodes();
			if (children != null)
			{
				for (int count = 0; count < children.getLength(); count++)
				{
					Node child = children.item(count);
					String childname = child.getNodeName();
					if (childname.equalsIgnoreCase(SERVICE))
					{
						UDPService service = createUDPService(loader, child);
						listener.setUDPService(service);
					}
					else if (childname.equalsIgnoreCase(FILTER))
					{
						createServiceFilter(loader, child);
					}
				}
			}
			
			return listener;
		}
		else throw new ServiceException("UDPListener must be created by a node named UDPListener!");
	}

	private UDPService createUDPService(ClassLoader loader, Node node)
	throws ServiceException
	{
		String nodeName = node.getNodeName();
		if (SERVICE.equalsIgnoreCase(nodeName))
		{
			UDPService service = (UDPService) parseConfigurationNode(loader, node);
			return service;
		}
		else throw new ServiceException("Service must be configured by a node named service!");
	}

	/*
	private Map<String,Domain> loadDomains(File directory) throws ServiceException
	{
		HashMap<String,Domain> domains = new HashMap<String,Domain>();
		if (directory.exists() && (directory.isDirectory()))
		{
			extractJars(directory);
			File[] domainFiles = directory.listFiles();
			
			// First, set up the classloader properly so everyone can see everyone else's api
			for (File domainFile : domainFiles)
			{
				if (domainFile.isDirectory())
				{
					System.out.println("Configuring domain: " + domainFile);
				}
			}

			// Now we configure the rest of the domain
			for (File domainFile : domainFiles)
			{
				if (domainFile.isDirectory())
				{
					File domainConfigFile = new File(domainFile + File.separator + "domain.xml");
					if (domainConfigFile.exists())
					{
						try
						{
							Domain domain = parseDomainConfig(domainLoader, domainConfigFile);
							DomainClassLoader domainLoader = DomainClassLoader.createDomainClassLoader(domainFile, apiClassLoader, domain);
							domains.put(domainFile.getName(), domain);
						}
						
						catch (Throwable e) 
						{
							e.printStackTrace();
							String msg = "Could not start domain from " + domainFile + ";";
							msg += domainConfigFile + " was not parsed successfully.";
							throw new ServiceException(msg);							
						}
					}
					else
					{
						String msg = "Could not start domain from " + domainFile + ";";
						msg += domainConfigFile + " does not exist.";
						throw new ServiceException(msg);
					}
				}
			}
		}
		return domains;
	}
	*/
	/*
	private Map<String,ServiceEngine> loadEngines(File engineDirectory) throws ServiceException
	{
		HashMap<String,ServiceEngine> result = new HashMap<String,ServiceEngine>();
		if (engineDirectory.exists() && (engineDirectory.isDirectory()))
		{
			extractJars(engineDirectory);
			File[] engineFiles = engineDirectory.listFiles();
			
			// First, set up the classloader properly so everyone can see everyone else's api
			for (File engineFile : engineFiles)
			{
				if (engineFile.isDirectory())
				{
					System.out.println("Configuring engine: " + engineFile);
					File apiDirectory = new File(engineFile + File.separator + "api");
					if (apiDirectory.exists()) apiClassLoader.addJarDirectory(apiDirectory);
				}
			}

			// Now we configure the rest of the engine
			for (File engineFile : engineFiles)
			{
				if (engineFile.isDirectory())
				{
					EngineClassLoader engineLoader = EngineClassLoader.createEngineClassLoader(engineFile, apiClassLoader);
					File engineConfigFile = new File(engineFile + File.separator + "engine.xml");
					if (engineConfigFile.exists())
					{
						try
						{
							ServiceEngine engine = parseEngineConfig(engineLoader, engineConfigFile);
							result.put(engineFile.getName(), engine);
						}
						
						catch (Throwable e) 
						{
							e.printStackTrace();
							String msg = "Could not start engine from " + engineFile + ";";
							msg += engineConfigFile + " was not parsed successfully.";
							throw new ServiceException(msg);							
						}
					}
					else
					{
						String msg = "Could not start engine from " + engineFile + ";";
						msg += engineConfigFile + " does not exist.";
						throw new ServiceException(msg);
					}
				}
			}
		}
		return result;
	}
	*/
	private Domain parseDomainConfig(DomainClassLoader loader, File configFile)
	throws Exception
	{
		DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(configFile);
		
		Domain domain = null;
		NodeList children = document.getChildNodes();
		for (int count = 0; count < children.getLength(); count++)
		{
			Node child = children.item(count);
			String name = child.getNodeName();
			if (name.equalsIgnoreCase("Domain"))
			{
				domain = createDomain(loader, child);
			}
		}
		
		return domain;
	}

	private ServiceEngine parseEngineConfig(EngineClassLoader loader, File configFile)
	throws Exception
	{
		DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(configFile);
		
		ServiceEngine engine = null;
		NodeList children = document.getChildNodes();
		for (int count = 0; count < children.getLength(); count++)
		{
			Node child = children.item(count);
			String name = child.getNodeName();
			if (name.equalsIgnoreCase(ENGINE))
			{
				engine = createServiceEngine(loader, child);
			}
		}
		
		return engine;
	}

	private Domain createDomain(DomainClassLoader loader, Node node)
	throws ServiceException
	{
		String nodename = node.getNodeName();
		if (nodename.equalsIgnoreCase(DOMAIN))
		{
			Domain domain = (Domain) parseConfigurationNode(loader, node);
			return domain;
		}
		else throw new ServiceException("Domain must be created by a node named domain!");
	}

	private ServiceEngine createServiceEngine(EngineClassLoader loader, Node node)
	throws ServiceException
	{
		String nodename = node.getNodeName();
		if (nodename.equalsIgnoreCase(ENGINE))
		{
			ServiceEngine engine = (ServiceEngine) parseConfigurationNode(loader, node);
			return engine;
		}
		else throw new ServiceException("Engine must be created by a node named engine!");
	}

	private AsyncService createAsyncService(ClassLoader loader, Node node) throws ServiceException
	{
		String nodeName = node.getNodeName();
		if (SERVICE.equalsIgnoreCase(nodeName))
		{
			AsyncService service = (AsyncService) parseConfigurationNode(loader, node);
			return service;
		}
		else throw new ServiceException("Service must be configured by a node named service!");
	}

	private SyncService createSyncService(ClassLoader loader, Node node) throws ServiceException
	{
		String nodeName = node.getNodeName();
		if (SERVICE.equalsIgnoreCase(nodeName))
		{
			SyncService service = (SyncService) parseConfigurationNode(loader, node);
			return service;
		}
		else throw new ServiceException("Service must be configured by a node named service!");
	}

	private ServiceFilter createServiceFilter(ClassLoader loader, Node node) throws ServiceException
	{
		String nodeName = node.getNodeName();
		if (FILTER.equalsIgnoreCase(nodeName))
		{
			ServiceFilter filter = (ServiceFilter) parseConfigurationNode(loader, node);
			return filter;
		}
		else throw new ServiceException("ServiceFilter must be configured by a node named filter!");		
	}

	/**
	 * Parses a configuration node and calls object.set[AttributeName]() 
	 * for each attribute defined in the node.
	 * @param node
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private Object parseConfigurationNode(Object result, Node node) throws ServiceException
	{
		NamedNodeMap attributes = node.getAttributes();
		try
		{
			for (int i = 0; i < attributes.getLength(); i++)
			{
				Node attribute = attributes.item(i);
				String attributeName = attribute.getNodeName();
				String attributeValue = attribute.getNodeValue();
				setNamedValue(result, attributeName, attributeValue);
			}
			
			return result;
		}
		
		catch (Exception e)
		{
			throw new ServiceException(e);
		}
	}

	/**
	 * Parses a configuration node and returns an object representation.
	 * @param node
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private Object parseConfigurationNode(ClassLoader loader, Node node) throws ServiceException
	{
		NamedNodeMap attributes = node.getAttributes();
		Node classNode = attributes.getNamedItem("class");
		if (classNode != null)
		{
			String className = classNode.getNodeValue();
			if ((className != null) && (className.length() > 0))
			{
				try
				{
					Object result = loader.loadClass(className).newInstance();
					for (int i = 0; i < attributes.getLength(); i++)
					{
						Node attribute = attributes.item(i);
						String attributeName = attribute.getNodeName();
						String attributeValue = attribute.getNodeValue();
						
						// Don't try to set the class attribute
						if (!attributeName.equalsIgnoreCase("class"))
						{
							try
							{
								setNamedValue(result, attributeName, attributeValue);
							}
							
							catch (Exception e)
							{
								// Continue setting other attributes even if one fails
								e.printStackTrace();
							}
						}
					}
					
					return result;
				}

				catch (ClassNotFoundException e)
				{
					System.err.println("Exception occurred loading class " + className);
					e.printStackTrace();
					throw new ServiceException(e);
				}

				catch (Exception e)
				{
					System.err.println("Unrecognized exception occurred instantiating or configuring " + className);
					e.printStackTrace();
					throw new ServiceException(e);
				}
			}
			else throw new ServiceException(node.getNodeName() + " requires a class attribute!");
		}
		else throw new ServiceException(node.getNodeName() + " requires a class attribute!");	
	}

	/**
	 * In future implementations, this will check for integer, float, etc.
	 * @param o
	 * @param name
	 * @param arg
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private static void setNamedValue(Object o, String name, String arg) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Class oClass = o.getClass();
		Method[] methods = oClass.getMethods();
		for (int i = 0; i < methods.length; i++)
		{
			String methodName = methods[i].getName();
			if (methodName.equalsIgnoreCase("set" + name))
			{
				Class<?>[] params = methods[i].getParameterTypes();
				if (params.length == 1)
				{
					if (params[0].isPrimitive()) 
					{
						if (params[0] == java.lang.Integer.TYPE)
						{
							try 
							{ 
								methods[i].invoke(o, Integer.parseInt(arg));
								return;
							}
							catch (NumberFormatException e) { }
						}
						else if (params[0] == java.lang.Double.TYPE)
						{
							try 
							{ 
								methods[i].invoke(o, Double.parseDouble(arg));
								return;
							}
							catch (NumberFormatException e) { }
						}
						else if (params[0] == java.lang.Float.TYPE)
						{
							try 
							{ 
								methods[i].invoke(o, Float.parseFloat(arg));
								return;
							}
							catch (NumberFormatException e) { }
						}
						else if (params[0] == java.lang.Long.TYPE)
						{
							try 
							{ 
								methods[i].invoke(o, Long.parseLong(arg));
								return;
							}
							catch (NumberFormatException e) { }
						}
						else if (params[0] == java.lang.Boolean.TYPE)
						{
							try 
							{ 
								methods[i].invoke(o, Boolean.parseBoolean(arg));
								return;
							}
							catch (NumberFormatException e) { }
						}
					}
					else if (params[0].getCanonicalName().equalsIgnoreCase("java.lang.String"))
					{
						try 
						{ 
							methods[i].invoke(o, arg);
							return;
						} 
						catch (Exception e) { }
					}
				}
			}
		}
	}

	/**
	 * Extracts the jar files contained within the specified directory,
	 * if they have not already been extracted.
	 * @param directory
	 */
	private void extractJars(File directory)
	{
		File[] jars = directory.listFiles(new JarFileFilter());
		for (File jar : jars)
		{
			try
			{
				File jardir = createJarDirectoryFile(jar);
				System.out.println("Extracting " + jar + " to " + jardir);
				JarArchive.extractJar(jar, jardir);
			}
			
			catch (java.io.IOException e)
			{
				System.out.println("Could not extract service from " + jar);
				e.printStackTrace();
			}
		}
	}

	private File createJarDirectoryFile(File jar)
	{
		String name = jar.getName();
		File parent = jar.getParentFile();
		if ((name.endsWith(".jar")) || (name.endsWith(".JAR")))
		{
			String resultName = name.substring(0, name.length() - 4);
			return new File(parent + File.separator + resultName);
		}
		else return jar;
	}
	
}
