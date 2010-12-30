package com.zeroturnaround.alvor.configuration;

public class DataSourceProperties {
	private String pattern;
	private String driverName;
	private String url;
	private String userName;
	private String password;

	public DataSourceProperties(String pattern, String driverName, String url, String userName, String password) {
		this.pattern = pattern;
		this.driverName = driverName;
		this.url = url;
		this.userName = userName;
		this.password = password;
	}
	
	public String getDriverName() {
		return driverName;
	}
	
	public String getPattern() {
		return pattern;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setPattern(String id) {
		this.pattern = id;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
