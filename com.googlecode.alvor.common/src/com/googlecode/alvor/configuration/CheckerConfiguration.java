package com.googlecode.alvor.configuration;

import java.util.List;

public class CheckerConfiguration {
	private List<String> patterns;
	private String checkerName;
	private String driverName;
	private String url;
	private String userName;
	private String password;

	public CheckerConfiguration(String checkerName, String driverName, String url, String userName, String password, List<String> patterns) {
		this.checkerName = checkerName;
		this.patterns = patterns;
		this.driverName = driverName;
		this.url = url;
		this.userName = userName;
		this.password = password;
	}
	
	public String getDriverName() {
		return driverName;
	}
	
	public List<String> getPatterns() {
		return patterns;
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
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CheckerConfiguration)) {
			return false;
		}
		CheckerConfiguration that = (CheckerConfiguration)obj;
		return this.patterns.equals(that.getPatterns())
				&& this.checkerName.equals(that.getCheckerName())
				&& this.driverName.equals(that.getDriverName())
				&& this.url.equals(that.getUrl())
				&& this.password.equals(that.getPassword())
				&& this.userName.equals(that.getUserName());
	}
	
	public boolean matchesPattern(String pattern) {
		return patterns.contains(pattern);
	}
	
	public String getCheckerName() {
		return checkerName;
	}
	
	@Override
	public int hashCode() {
		int result = 1;
		result = result * 31 + checkerName.hashCode();
		result = result * 31 + patterns.hashCode();
		result = result * 31 + driverName.hashCode();
		result = result * 31 + url.hashCode();
		result = result * 31 + userName.hashCode();
		result = result * 31 + password.hashCode();
		return result;
	}

	/**
	 * Default checker is used when no other checker matches 
	 * @return
	 */
	public boolean isDefaultChecker() {
		return this.patterns.isEmpty();
	}
}
