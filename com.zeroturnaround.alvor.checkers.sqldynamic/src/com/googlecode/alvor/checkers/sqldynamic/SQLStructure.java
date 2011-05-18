package com.googlecode.alvor.checkers.sqldynamic;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;

/**
 * Describes structure of a SQL statement / result
 * or error message
 * 
 * Used for resultset analysis
 */
public class SQLStructure {
	public ResultSetMetaData resultSetMD;
	public ParameterMetaData parameterMD;
	public String errorMsg;
	
	public SQLStructure(ResultSetMetaData rsMD, ParameterMetaData pMD) {
		this.resultSetMD = rsMD;
		this.parameterMD = pMD;
	}
	
	public SQLStructure(Exception e) {
		errorMsg = e.getMessage().replace("\n", "; ");
	}
}
