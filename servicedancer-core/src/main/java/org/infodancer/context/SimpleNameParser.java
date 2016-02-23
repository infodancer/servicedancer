package org.infodancer.context;

import javax.naming.NameParser;
import javax.naming.Name;
import javax.naming.CompoundName;
import javax.naming.NamingException;
import java.util.Properties;

public class SimpleNameParser implements NameParser {
	static Properties syntax = new Properties();
	static 
	{
		syntax.put("jndi.syntax.direction", "flat");
		syntax.put("jndi.syntax.ignorecase", "false");
	}
	
	public Name parse(String name) throws NamingException {
		return new CompoundName(name, syntax);
	}
}
