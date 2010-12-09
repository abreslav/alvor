package com.zeroturnaround.alvor.configuration;

public class DataSourceProperties {
	private final String id;
	private final String driverName;
	private final String url;
	private final String userName;
	private final String password;

	public DataSourceProperties(String id, String driverName, String url, String userName, String password) {
		this.id = id;
		this.driverName = driverName;
		this.url = url;
		this.userName = userName;
		this.password = password;
	}
	
	public String getDriverName() {
		return driverName;
	}
	
	public String getId() {
		return id;
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
}
