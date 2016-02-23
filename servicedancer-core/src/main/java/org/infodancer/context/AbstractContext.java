package org.infodancer.context;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.spi.NamingManager;

public abstract class AbstractContext implements Context
{
	protected Hashtable<String,Object> environment;
	protected NameParser myParser = new SimpleNameParser();
	
	protected AbstractContext(Hashtable environment) 
	{
		if (environment != null) environment = (Hashtable) environment.clone();
		else environment = null;
	}
	
	/**
	 * Provides a reference to the backing store of this Context.
	 * Non-abstract subclasses are expected to implement this method. 
	 */
	protected abstract Map<String,Object> getBindings();
	
	/**
	 * Provides a new instance of this Context.
	 * Non-abstract subclasses are expected to implement this method. 
	 * @return Context
	 */
	protected abstract Context getNewInstance(Hashtable env);
	
	public Object lookup(String name) throws NamingException {
		return lookup(new CompositeName(name));
	}

	public Object lookup(Name name) throws NamingException {
		if (name.equals("")) {
			// Asking to look up this context itself. Create and return
			// a new instance with its own independent environment.
			return getNewInstance(environment);
		}
		Object answer = getBindings().get(name.toString());
		if (answer == null) throw new NameNotFoundException(name + " not found");
		else if (answer instanceof Reference) 
		{
			try 
			{
				return NamingManager.getObjectInstance(answer, name, this, null);
			} 
			
			catch (Exception e) 
			{
				e.printStackTrace();
				throw new NamingException();
			}
		}
		else return answer;
	}

	public void bind(String name, Object obj) throws NamingException {
		if (name.equals("")) {
			throw new InvalidNameException("Cannot bind empty name");
		}
		if (getBindings().get(name) != null) {
			throw new NameAlreadyBoundException(name + " already bound, use rebind to override.");
		}
		getBindings().put(name, obj);
	}

	public void bind(Name name, Object obj) throws NamingException {
		// Flat namespace; no federation; just call string version
		bind(name.toString(), obj);
	}

	public void rebind(String name, Object obj) throws NamingException {
		if (name.equals("")) {
			throw new InvalidNameException("Cannot bind empty name");
		}
		getBindings().put(name, obj);
	}

	public void rebind(Name name, Object obj) throws NamingException {
		// Flat namespace; no federation; just call string version
		rebind(name.toString(), obj);
	}

	public void unbind(String name) throws NamingException {
		if (name.equals("")) {
			throw new InvalidNameException("Cannot unbind empty name");
		}
		getBindings().remove(name);
	}

	public void unbind(Name name) throws NamingException {
		// Flat namespace; no federation; just call string version
		unbind(name.toString());
	}

	public void rename(String oldname, String newname) throws NamingException {
		if (oldname.equals("") || newname.equals("")) {
			throw new InvalidNameException("Cannot rename empty name");
		}
		// Check if new name exists
		if (getBindings().get(newname) != null) {
			throw new NameAlreadyBoundException(newname + " is already bound");
		}
		// Check if old name is bound
		Object oldBinding = getBindings().remove(oldname);
		if (oldBinding == null) {
			throw new NameNotFoundException(oldname + " not bound");
		}
		getBindings().put(newname, oldBinding);
	}

	public void rename(Name oldname, Name newname) throws NamingException {
		// Flat namespace; no federation; just call string version
		rename(oldname.toString(), newname.toString());
	}

	public NamingEnumeration list(String name) throws NamingException {
		if (name.equals("")) {
			// listing this context
			return new FlatNames(getBindings().keySet());
		}
		// Perhaps 'name' names a context
		Object target = lookup(name);
		if (target instanceof Context) {
			return ((Context) target).list("");
		}
		throw new NotContextException(name + " cannot be listed");
	}

	public NamingEnumeration list(Name name) throws NamingException {
		// Flat namespace; no federation; just call string version
		return list(name.toString());
	}

	public NamingEnumeration listBindings(String name) throws NamingException {
		if (name.equals("")) {
			// listing this context
			return new FlatBindings(getBindings().keySet());
		}
		// Perhaps 'name' names a context
		Object target = lookup(name);
		if (target instanceof Context) {
			return ((Context) target).listBindings("");
		}
		throw new NotContextException(name + " cannot be listed");
	}

	public NamingEnumeration listBindings(Name name) throws NamingException {
		// Flat namespace; no federation; just call string version
		return listBindings(name.toString());
	}

	public void destroySubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException(
				"FlatCtx does not support subcontexts");
	}

	public void destroySubcontext(Name name) throws NamingException {
		// Flat namespace; no federation; just call string version
		destroySubcontext(name.toString());
	}

	public Context createSubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException(
				"FlatCtx does not support subcontexts");
	}

	public Context createSubcontext(Name name) throws NamingException {
		// Flat namespace; no federation; just call string version
		return createSubcontext(name.toString());
	}

	public Object lookupLink(String name) throws NamingException {
		// This flat context does not treat links specially
		return lookup(name);
	}

	public Object lookupLink(Name name) throws NamingException {
		// Flat namespace; no federation; just call string version
		return lookupLink(name.toString());
	}

	public NameParser getNameParser(String name) throws NamingException {
		return myParser;
	}

	public NameParser getNameParser(Name name) throws NamingException {
		// Flat namespace; no federation; just call string version
		return getNameParser(name.toString());
	}

	public String composeName(String name, String prefix)
			throws NamingException {
		Name result = composeName(new CompositeName(name), new CompositeName(
				prefix));
		return result.toString();
	}

	public Name composeName(Name name, Name prefix) throws NamingException {
		Name result = (Name) (prefix.clone());
		result.addAll(name);
		return result;
	}

	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		if (environment == null) {
			environment = new Hashtable(5, 0.75f);
		}
		return environment.put(propName, propVal);
	}

	public Object removeFromEnvironment(String propName) throws NamingException {
		if (environment == null)
			return null;
		return environment.remove(propName);
	}

	public Hashtable getEnvironment() throws NamingException {
		if (environment == null) {
			// Must return non-null
			return new Hashtable(3, 0.75f);
		} else {
			return (Hashtable) environment.clone();
		}
	}

	public String getNameInNamespace() throws NamingException {
		return "";
	}

	public void close() throws NamingException 
	{
		environment = null;
	}

	// Class for enumerating name/class pairs
	class FlatNames implements NamingEnumeration 
	{
		java.util.Iterator names;

		FlatNames(java.util.Set<String> names) 
		{
			this.names = names.iterator();
		}

		public boolean hasMoreElements()
		{
			return names.hasNext();
		}

		public boolean hasMore() throws NamingException 
		{
			return hasMoreElements();
		}

		public Object nextElement() 
		{
			String name = (String) names.next();
			String className = getBindings().get(name).getClass().getName();
			return new NameClassPair(name, className);
		}

		public Object next() throws NamingException 
		{
			return nextElement();
		}

		public void close() { }
	}

	// Class for enumerating getBindings()
	class FlatBindings implements NamingEnumeration 
	{
		java.util.Iterator names;

		FlatBindings(java.util.Set<String> names) 
		{
			this.names = names.iterator();
		}

		public boolean hasMoreElements() 
		{
			return names.hasNext();
		}

		public boolean hasMore() throws NamingException 
		{
			return hasMoreElements();
		}

		public Object nextElement() 
		{
			String name = (String) names.next();
			return new Binding(name, getBindings().get(name));
		}

		public Object next() throws NamingException 
		{
			return nextElement();
		}

		public void close() { }
	}
}
