/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service.filter;

import java.net.*;
import java.util.regex.*;
import javax.naming.*;
import javax.naming.directory.*;

import org.w3c.dom.*;

/** 
 * This class looks up the connecting IP address in a configured RBL, 
 * and allows the connection only if the IP address is not listed, or if
 * the 
 **/

public class DNSBLServiceFilter
{
	static Pattern pattern;
	String zone;
	int value;
	
	static 
	{
		pattern = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
	}
	
	public DNSBLServiceFilter(Node node)
	{
		this.value = -1;
		this.zone = "";
	}
	
	public static InetAddress lookup(String domain, InetAddress host) throws javax.naming.NamingException
	{
		return lookup(domain, host.toString());
	}
	
	public static InetAddress lookup(String domain, String host) throws javax.naming.NamingException
	{
		try
		{
			Matcher matcher = pattern.matcher(host);
			if (matcher.matches())
			{
				StringBuffer lookupName = new StringBuffer();
				lookupName.append("dns://localhost/");
				lookupName.append(matcher.group(4));
				lookupName.append(".");
				lookupName.append(matcher.group(3));
				lookupName.append(".");
				lookupName.append(matcher.group(2));
				lookupName.append(".");
				lookupName.append(matcher.group(1));
				lookupName.append(".");
				lookupName.append(domain);
				
				DirContext ictx = new InitialDirContext();
				Attributes attributes = ictx.getAttributes(lookupName.toString(), new String[] {"A"});
				NamingEnumeration e = attributes.getAll();
				while (e.hasMore())
				{
					Attribute a = (Attribute) e.next();
					return InetAddress.getByName(a.get().toString());
				}
			}
			return null;
		}
		
		catch (UnknownHostException e)
		{
			return null;
		}

		catch (NameNotFoundException e)
		{
			return null;
		}
	}
	
	public int permit(InetAddress address)
	{
		try
		{
			InetAddress result = lookup(zone, address);
			if (result != null) 
			{
				return value;
			}
			else return 0;
		}
		
		catch (javax.naming.NamingException e)
		{
			System.err.println("Exception looking up " + address + " in " + zone);
			e.printStackTrace();
			return 0;
		}
	}
	
	public static final void main(String[] args)
	{
		for (int count = 1; count < args.length; count++)
		{
			try
			{
				System.out.print("Looking up " + args[count] + ": ");
				InetAddress result = DNSBLServiceFilter.lookup(args[0], args[count]); 
				if (result != null) System.out.println(result.toString()); 
				else System.out.println("Not listed!");
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
