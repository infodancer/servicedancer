package org.infodancer.context;

import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.infodancer.service.api.ServiceException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ContextParser 
{	
	private static final Logger log = Logger.getLogger(ContextParser.class.getName());
	
	public static void parseContextFile(Context context, java.io.File file) throws ServiceException
	{
		try
		{
			if (file.exists())
			{
				log.fine("Parsing context file: " + file.getAbsolutePath());
				DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(file);
				parseDocumentNode(context, document);
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			log.warning("Could not parse context file: " + file.getAbsolutePath());
			throw new ServiceException("Could not parse context file!", e);
		}
	}
	
	private static void parseDocumentNode(Context context, Node node) throws Exception
	{
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			String childname = child.getNodeName();
			if ("context".equalsIgnoreCase(childname))
			{
				parseContextNode(context, child);
			}
			else parseDocumentNode(context, child);
		}		
	}
	
	private static void parseContextNode(Context context, Node node) throws Exception
	{
		String name = node.getNodeName();
		if (name.equalsIgnoreCase("context"))
		{
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				Node child = children.item(i);
				String childname = child.getNodeName();
				if ("resource".equalsIgnoreCase(childname))
				{
					parseResourceNode(context, child);
				}
				else if ("environment".equalsIgnoreCase(childname))
				{
					parseEnvironmentNode(context, child);
				}
			}
		}
	}
	
	/**
	 * Transform a Resource node into a Context binding.
	 * @param context
	 * @param node
	 * @throws Exception
	 */
	private static void parseResourceNode(Context context, Node node) throws Exception
	{
		String name = node.getNodeName();
		if (name.equalsIgnoreCase("resource"))
		{
			NamedNodeMap attributes = node.getAttributes();
			Node typeNameNode = attributes.getNamedItem("type");
			Node contextNameNode = attributes.getNamedItem("name");
			Node factoryNameNode = attributes.getNamedItem("factory");
			
			String contextName = null;
			if (contextNameNode != null) 
			{
				contextName = contextNameNode.getNodeValue();
			}
			else throw new ServiceException("No context name defined for this resource!");
			
			String factoryName = null;
			if (factoryNameNode != null) 
			{
				factoryName = factoryNameNode.getNodeValue();
			}
			else factoryName = "org.infodancer.context.BeanFactory";
			
			String typeName = null;
			if (typeNameNode != null)
			{
				typeName = typeNameNode.getNodeValue();                                                                                                                                                                                                               
			}
			else throw new ServiceException("No type defined for this resource!");
			
			if ((contextName != null) && (typeName != null))
			{
				Reference ref = new Reference(typeName, factoryName, null);
				parseAttributesToReference(ref, node);
				context.bind(contextName, ref);
			}
		}		
		else throw new ServiceException("Unrecognized element " + name + "!");
	}
	
	private static void parseAttributesToReference(Reference ref, Node node)
	{
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++)
		{
			Node attributeNode = attributes.item(i);
			String attributeName = attributeNode.getNodeName();
			String attributeValue = attributeNode.getNodeValue();
			ref.add(new StringRefAddr(attributeName, attributeValue));
		}
	}
	
	private static void parseEnvironmentNode(Context context, Node node) throws Exception
	{
		String name = node.getNodeName();
		if (name.equalsIgnoreCase("environment"))
		{
			NamedNodeMap attributes = node.getAttributes();
			Node typeNameNode = attributes.getNamedItem("type");
			Node contextNameNode = attributes.getNamedItem("name");
			Node factoryClassNameNode = attributes.getNamedItem("factory");
			
			String contextName = null;
			if (contextNameNode != null) 
			{
				contextName = contextNameNode.getNodeValue();
			}
			else throw new ServiceException("No context name defined for this resource!");
			
			String typeName = null;
			if (typeNameNode != null)
			{
				typeName = typeNameNode.getNodeValue();
			}
			else throw new ServiceException("No type defined for this resource!");
			
			String factoryClassName = null;
			if (factoryClassNameNode != null)
			{
				factoryClassName = factoryClassNameNode.getNodeValue();
			}
			else factoryClassName = "org.infodancer.context.BeanFactory";
			
			if ((contextName != null) && (typeName != null))
			{
				Reference ref = new Reference(typeName, factoryClassName, null);	
				for (int i = 0; i < attributes.getLength(); i++)
				{
					Node attribute = attributes.item(i);
					 
					String attributeName = attribute.getNodeName();
					String attributeValue = attribute.getNodeValue();
					if ((!"factory".equalsIgnoreCase(attributeName)) && 
						(!"type".equalsIgnoreCase(attributeName)))
					{
						StringRefAddr addr = new StringRefAddr(attributeName, attributeValue);
						ref.add(addr);
					}
				}
				context.bind(contextName, ref);
			}			
		}
		else throw new ServiceException("Unrecognized element " + name + "!");
	}
}
