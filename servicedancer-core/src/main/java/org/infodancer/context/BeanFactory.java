package org.infodancer.context;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class BeanFactory implements ObjectFactory 
{
	private static final Logger log = Logger.getLogger(BeanFactory.class.getName());
	
	/**
     * Create a new Bean instance.
     * This implementation attempts to gracefully handle non-fatal errors,
     * such as properties that cannot be set.  
     * 
     * @param obj The reference object describing the Bean
     */
    
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
	throws NamingException 
    {
        if (obj instanceof Reference)
        {
            try 
            {
                Reference ref = (Reference) obj;
                String beanClassName = ref.getClassName();
                Class beanClass = null;
                ClassLoader tcl = Thread.currentThread().getContextClassLoader();
                if (tcl != null) 
                {
                    try 
                    {	
                        beanClass = tcl.loadClass(beanClassName);
                    }
                    
                    catch(ClassNotFoundException e) 
                    {
                    	e.printStackTrace();
                    }
                } 
                else 
                {
                    try 
                    {
                        beanClass = Class.forName(beanClassName);
                    }
                    
                    catch(ClassNotFoundException e) 
                    {
                        e.printStackTrace();
                    }
                }
            
                if (beanClass == null) 
                {
                    throw new NamingException("Class not found: " + beanClassName);
                }
                
                BeanInfo bi = Introspector.getBeanInfo(beanClass);
                PropertyDescriptor[] pda = bi.getPropertyDescriptors();
                
                Object bean = beanClass.newInstance();
                
                Enumeration e = ref.getAll();
                while (e.hasMoreElements()) {
                    
                    RefAddr ra = (RefAddr) e.nextElement();
                    String propName = ra.getType();
                    
                    if (propName.equalsIgnoreCase("factory")) continue;
                    
                    String value = (String)ra.getContent();
                    
                    Object[] valueArray = new Object[1];
                    
                    int i = 0;
                    for (i = 0; i<pda.length; i++) {
                        if (pda[i].getName().equals(propName)) {
                            Class propType = pda[i].getPropertyType();
                            if (propType.equals(String.class)) {
                                valueArray[0] = value;
                            } else if (propType.equals(Character.class) 
                                       || propType.equals(char.class)) {
                                valueArray[0] = new Character(value.charAt(0));
                            } else if (propType.equals(Byte.class) 
                                       || propType.equals(byte.class)) {
                                valueArray[0] = new Byte(value);
                            } else if (propType.equals(Short.class) 
                                       || propType.equals(short.class)) {
                                valueArray[0] = new Short(value);
                            } else if (propType.equals(Integer.class) 
                                       || propType.equals(int.class)) {
                                valueArray[0] = new Integer(value);
                            } else if (propType.equals(Long.class) 
                                       || propType.equals(long.class)) {
                                valueArray[0] = new Long(value);
                            } else if (propType.equals(Float.class) 
                                       || propType.equals(float.class)) {
                                valueArray[0] = new Float(value);
                            } else if (propType.equals(Double.class) 
                                       || propType.equals(double.class)) {
                                valueArray[0] = new Double(value);
                            } else if (propType.equals(Boolean.class)
                                       || propType.equals(boolean.class)) {
                                valueArray[0] = new Boolean(value);
                            } else {
                            	String error = "String conversion for property " + propName + "<" + propType + "> not available."; 
                            	log.warning(error);
                            }
                            
                            Method setProp = pda[i].getWriteMethod();
                            if (setProp != null) 
                            {
                                setProp.invoke(bean, valueArray);
                            } 
                            else 
                            {
                                log.warning("Write not allowed for property: " + propName);
                            }

                            break;

                        }

                    }

                    if (i == pda.length) 
                    {
                    	log.warning("No set method found for property: " + propName);
                    }
                }
                return bean;
            } 
            
            catch (java.beans.IntrospectionException ie) 
            {
                NamingException ne = new NamingException(ie.getMessage());
                ne.setRootCause(ie);
                throw ne;
            } 
            
            catch (java.lang.IllegalAccessException iae) 
            {
                NamingException ne = new NamingException(iae.getMessage());
                ne.setRootCause(iae);
                throw ne;
            } 
            
            catch (java.lang.InstantiationException ie2) {
            	ie2.printStackTrace();
                NamingException ne = new NamingException(ie2.getMessage());
                ne.setRootCause(ie2);
                throw ne;
            } 
            
            catch (java.lang.reflect.InvocationTargetException ite) 
            {
                NamingException ne = new NamingException(ite.getMessage());
                ne.setRootCause(ite);
                throw ne;
            }
        } 
        else 
        {
            return null;
        }
    }
}
