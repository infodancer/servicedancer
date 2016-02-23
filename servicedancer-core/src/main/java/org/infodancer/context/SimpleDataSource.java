package org.infodancer.context;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class SimpleDataSource implements DataSource 
{
	int loginTimeout;
	PrintWriter logWriter;
	String username;
	String password;
	String url;
	String driver;
	Class<?> driverClass;
	Driver driverInstance;
	
	public SimpleDataSource()
	{
		
	}
	
	public SimpleDataSource(String driver, String url)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		this.url = url;
		this.driver = driver;
		this.driverClass = Class.forName(driver);
		this.driverInstance = (java.sql.Driver) driverClass.newInstance();
	}
	
	public SimpleDataSource(String driver, String url, String username, String password)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		this(driver,url);
		this.username = username;
		this.password = password;
	}
	
	public Connection getConnection() throws SQLException 
	{
		return DriverManager.getConnection(url, username, password);
	}

	public Connection getConnection(String username, String password)
	throws SQLException 
	{
		return DriverManager.getConnection(url, username, password);
	}

	public PrintWriter getLogWriter() throws SQLException 
	{
		return logWriter;
	}

	public int getLoginTimeout() throws SQLException 
	{
		return loginTimeout;
	}

	public void setLogWriter(PrintWriter out) throws SQLException 
	{
		this.logWriter = out;
	}

	public void setLoginTimeout(int seconds) throws SQLException 
	{
		this.loginTimeout = seconds;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException 
	{
		if (driverClass.equals(iface)) return true;
		else return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException 
	{
		return null;
	}

	public String getUsername() 
	{
		return username;
	}

	public void setUsername(String username) 
	{
		this.username = username;
	}

	public String getPassword() 
	{
		return password;
	}

	public void setPassword(String password) 
	{
		this.password = password;
	}

	public String getUrl() 
	{
		return url;
	}

	public void setUrl(String url) 
	{
		this.url = url;
	}

	public String getDriverClassName() 
	{
		return driver;
	}

	public void setDriverClassName(String driver) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		this.driver = driver;
		this.driverClass = Class.forName(driver);
		this.driverInstance = (java.sql.Driver) driverClass.newInstance();		
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}
}
