package com.vnetcon.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;


public class JsonDriver implements Driver {

	private static Driver registeredDriver;
	
	private static final String dbConf = "/etc/vnetcon/database.properties";
	private static final String urlPrefix = "jdbc:vnetcon:json://";
	private Driver targetDriver = null;
	private Connection targetCon = null;
	private String targetURL = null;
	private Properties dbProps = new Properties();
		
	
	static {
		try {
			register();
		} catch (SQLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	
	public static void register() throws SQLException {
		if (isRegistered()) {
			throw new IllegalStateException("Driver is already registered. It can only be registered once.");
		}
		Driver registeredDriver = new JsonDriver();
		DriverManager.registerDriver(registeredDriver);
		JsonDriver.registeredDriver = registeredDriver;
	}
	
	public static void deregister() throws SQLException {
		if (!isRegistered()) {
			throw new IllegalStateException(
					"Driver is not registered (or it has not been registered using Driver.register() method)");
		}
		DriverManager.deregisterDriver(registeredDriver);
		registeredDriver = null;
	}
	
	public static boolean isRegistered() {
		return registeredDriver != null;
	}
	
	private String getDatabaseConfigString(String url) throws SQLException {
		try {
			String tmp  = url;
			tmp = tmp.replaceFirst(urlPrefix, "");
			if(tmp.indexOf('?') > -1) {
				String p[] = tmp.split("\\?");
				return p[0];
			} else {
				return tmp;
			}
		}catch(Exception e) {
			throw new SQLException(e);
		}
	}
	
	private Map<String, String> getUrlParams(String url) throws SQLException {
		Map<String, String> params = new HashMap<String, String>();
		try {
			String tmp  = url;
			tmp = tmp.replaceFirst(urlPrefix, "");
			String p[] = tmp.split("?");
			p = p[1].split("&");
			for(int i = 0; i < p.length; i++) {
				String pp[] = p[i].split("=");
				params.put(pp[0], pp[1]);
			}
		}catch(Exception e) {
			throw new SQLException(e);
		}
		return params;
	}
	
	private void loadProperties() throws Exception {
		FileInputStream fIn = new FileInputStream(dbConf);
		dbProps.load(fIn);
		fIn.close();
	}
	
	private Connection jsonConnect(String url, Properties info) throws SQLException {
		String dbConfig = this.getDatabaseConfigString(url);
		Json2DBConverter conv = new Json2DBConverter(dbConfig, dbProps);
		Map<String, String> params = this.getUrlParams(url);
		String jdbcUrl = dbProps.getProperty(dbConfig + ".jdbc.url");
		String jdbcUser = dbProps.getProperty(dbConfig + ".jdbc.user");
		String jdbcPass = dbProps.getProperty(dbConfig + ".jdbc.pass");
		String fileEncoding = params.get("encoding");
		String fileUrl = params.get("url");
		String httpUser = params.get("httpuser");
		String httpPass = params.get("httppass");
		String httpFile = params.get("httpfile");
		String dbschema = params.get("dbschema");
		String curlUser = null;
		
		targetCon = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
		targetURL = targetCon.getMetaData().getURL();
		targetDriver = DriverManager.getDriver(targetURL);
		
		conv.setConnection(targetCon);
		
		if(fileEncoding == null) {
			fileEncoding = "UTF-8";
		}
		try {
			if(fileUrl.startsWith("http")) {
				if(httpUser != null) {
					curlUser = httpUser + ":" + httpPass;
				}
				conv.makeHttpGetRequest(fileUrl, fileEncoding, curlUser, httpFile);
			}else {
				File folder = new File(fileUrl);
				for(File f : folder.listFiles()) {
					if(f.getName().toLowerCase().endsWith(".xml") || f.getName().toLowerCase().endsWith(".json")) {
						conv.parseFile(f, fileEncoding, dbschema);
					}
				}
			}
		} catch (Exception e) {
			throw new SQLException(e);
		}
		return targetCon;
	}
	
	public Connection connect(String url, Properties info) throws SQLException {
		
		try {
			if(url != null && url.startsWith(urlPrefix)) {
				this.loadProperties();
				return jsonConnect(url, info);
			}
		}catch(Exception e) {
			throw new SQLException(e);
		}
		
		throw new SQLException("No handler for given url");
	}

	/**
	 * jdbc:vnetcon:json://default?url=afdasfd&encoding=UTF-8&httpuser=user&httppass=asdfa
	 */
	public boolean acceptsURL(String url) throws SQLException {
		
		if(url != null && url.startsWith(urlPrefix)) {
			return true;
		}
		
		return false;
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return targetDriver.getPropertyInfo(url, info);
	}

	public int getMajorVersion() {
		return targetDriver.getMajorVersion();
	}

	public int getMinorVersion() {
		return targetDriver.getMinorVersion();
	}

	public boolean jdbcCompliant() {
		return targetDriver.jdbcCompliant();
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return targetDriver.getParentLogger();
	}

}
