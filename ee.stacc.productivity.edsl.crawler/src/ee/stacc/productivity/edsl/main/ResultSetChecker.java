package ee.stacc.productivity.edsl.main;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;

public class ResultSetChecker {
	public static void checkUsages(IJavaElement[] scope) {
		checkSignature(scope, "java.sql.ResultSet.getArray");
		checkSignature(scope, "java.sql.ResultSet.getAsciiStream");
		checkSignature(scope, "java.sql.ResultSet.getBigDecimal");
		checkSignature(scope, "java.sql.ResultSet.getBinaryStream");
		checkSignature(scope, "java.sql.ResultSet.getBlob");
		checkSignature(scope, "java.sql.ResultSet.getBoolean");
		checkSignature(scope, "java.sql.ResultSet.getByte");
		checkSignature(scope, "java.sql.ResultSet.getBytes");
		checkSignature(scope, "java.sql.ResultSet.getCharacterStream");
		checkSignature(scope, "java.sql.ResultSet.getClob");
		checkSignature(scope, "java.sql.ResultSet.getDate");
		checkSignature(scope, "java.sql.ResultSet.getDouble");
		checkSignature(scope, "java.sql.ResultSet.getInt");
		checkSignature(scope, "java.sql.ResultSet.getLong");
		checkSignature(scope, "java.sql.ResultSet.getNCharacterStream");
		checkSignature(scope, "java.sql.ResultSet.getNClob");
		checkSignature(scope, "java.sql.ResultSet.getNString");
		checkSignature(scope, "java.sql.ResultSet.getObject");
		checkSignature(scope, "java.sql.ResultSet.getRef");
		checkSignature(scope, "java.sql.ResultSet.getRowId");
		checkSignature(scope, "java.sql.ResultSet.getShort");
		checkSignature(scope, "java.sql.ResultSet.getSQLXML");
		checkSignature(scope, "java.sql.ResultSet.getString");
		checkSignature(scope, "java.sql.ResultSet.getTime");
		checkSignature(scope, "java.sql.ResultSet.getTimestamp");
		checkSignature(scope, "java.sql.ResultSet.getUnicodeStream");
		checkSignature(scope, "java.sql.ResultSet.getURL");
	}
	
	private static void checkSignature(IJavaElement[] scope, String signature) {
		List<String> nodes = NodeSearchEngine.findMethodInvocations(scope, signature);
		for (String node : nodes) {
			System.out.println(node);
		}
	}
}
