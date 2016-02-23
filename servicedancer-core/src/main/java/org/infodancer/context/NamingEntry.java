package org.infodancer.context;

import javax.naming.Reference;

public class NamingEntry
{
	public enum Type { ENTRY, REFERENCE, LINK_REF, CONTEXT }
	final Type type;
    final String name;
    final Object value;
	
    public NamingEntry(String name, Object value, Type type) 
    {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public Object getValue()
    {
    	return value;
    }
    
    public boolean equals(Object obj) 
    {
        if ((obj != null) && (obj instanceof NamingEntry)) 
        {
            return name.equals(((NamingEntry) obj).name);
        } 
        else 
        {
            return false;
        }
    }

    public int hashCode() 
    {
        return name.hashCode();
    }    
}
