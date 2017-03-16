package org.infodancer.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ServiceUtility 
{
	/**
	 * This method will accept an integer argument in string form, or if the argument is not an integer,
	 * attempt to look it up in /etc/services as a named port.
	 * @param s
	 * @return
	 */
	public static int validatePortArgument(String s) throws NumberFormatException
	{
		if ((s != null) && (!s.isEmpty()))
		{
			try
			{
				return Integer.parseInt(s);
			}
			
			catch (NumberFormatException e)
			{
				try
				{
					File services = new File("/etc/services");
					if (services.exists())
					{
						BufferedReader reader = null;
						
						try
						{
							reader = new BufferedReader(new FileReader(services));
							String line = null;
							while ((line = reader.readLine()) != null)
							{
								// Skip comments
								if (!line.startsWith("#"))
								{
									String[] tokens = line.split(" ");
									if ((s.equalsIgnoreCase(tokens[0])) && (tokens.length > 1))
									{
										return Integer.parseInt(tokens[1]);
									}
								}
							}
							
						}
						
						finally
						{
							try { if (reader != null) reader.close(); } catch (Exception ee) { } 
						}
					}
					
					throw new NumberFormatException("Could not locate service " + s + " in /etc/services!");
				}
				
				catch (Exception ee)
				{
					ee.printStackTrace();
					throw new NumberFormatException("Could not locate service " + s + " in /etc/services!");
				}
			}
		}
		else throw new NumberFormatException("Must provide either a port number or a named port listed in /etc/services!");
	}

}
