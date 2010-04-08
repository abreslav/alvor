package ee.stacc.productivity.edsl.testproject;

import java.sql.*;

public class TestProjectMain {
	String url = "jdbc:mysql://localhost:3306/openbravopos";
	Connection conn;

	public static void main(String[] args) throws Exception {
		TestProjectMain t = new TestProjectMain();
		try {
			t.createConnection();
		} catch (Exception e) {}
		
		//t.testOracle();
		t.testEArved();
		/*
		t.testVariablesAndResultSet();
		t.testArguments("customers");
		t.testArguments("ad_org");
		*/
	}
	
	void createConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(url, "openbravopos", "openbravopos");
	}
	
	static String getWhereClause() {
		return "where createds is null";
	}
	
	static int getSomeInt() {
		return 1000;
	}
	
	static boolean someCondition() {
		return false; 
	}
	
	void testInt() throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("select * from dual where id > "
				+ getSomeInt());
	}
	
	void testEArved() throws SQLException {
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		try {
			Class.forName ("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Connection conn = DriverManager.getConnection(url, "e_arved", "e_arved");
		// @machineName:port:SID,   userid,  password
		PreparedStatement stmt = conn.prepareStatement("select * from dual");
		stmt.getMetaData();
		
		/*
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery("select * from billing_planaa");
		while (rset.next())
			System.out.println (rset.getString(1));   // Print col 1
		stmt.close();
		*/

	}
	
	void testArguments(String tableName) throws Exception {
		String sql = "select id, name from " + tableName;
		PreparedStatement stmt = conn.prepareStatement(sql);
	}
	
	void testMethodInv() throws Exception {
		String sql = "select ad_org_id, name from ad_org " + getWhereClause();
		PreparedStatement stmt = conn.prepareStatement(sql);
	}
	
	void testVariablesAndResultSet() throws Exception {
		
		// choice of different fields of same type 
		String addrField = "address";
		if (someCondition()) {
			addrField = "address2";
		}
		
		addrField = "loll";
		
		String sql = "select id, name, " + addrField + " dt_fld from customers";
		sql += " order by " + addrField;
		
		sql = "a";
		sql += "b";
		
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.getMetaData();
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			// another possibility for content assist + error checking
			System.out.println(rs.getString("name"));
			System.out.println(rs.getString("namme"));
			System.out.println(rs.getInt("name"));
		}
	}
	
	void testStringBuilder() throws Exception {
		StringBuilder sb = new StringBuilder("select id, name ");
		sb.append("from customers ");
		if (someCondition()) {
			sb.append("where id is not null"); 
		}
		
		PreparedStatement stmt = conn.prepareStatement(sb.toString());
	}
	
	void testStringBuilder2() throws Exception {
		StringBuilder sb = new StringBuilder("a");
		sb.append("urraa");
		sb.append("b").append("c");
		PreparedStatement stmt = conn.prepareStatement(sb.toString());
	}
	
	void testStringBuilder3() throws Exception {
		StringBuilder sb = new StringBuilder("a").append("x");
		sb.append("urraa");
		StringBuilder sb2 = new StringBuilder();
		sb2.append("eee");
		sb2.append("uuu").append("rrr").append("ddd");
		sb.append("b").append("c");
		PreparedStatement stmt = conn.prepareStatement(sb.toString());
	}
	
	void testStringBuilder4() throws Exception {
		StringBuilder sb = new StringBuilder(200).append("x");
		sb.append("urraa");
		sb.append("b").append("c");
		PreparedStatement stmt = conn.prepareStatement(sb.toString());
	}
	
	void testPrepareStatementInLoop() {
		// Compiere, MSequence.java:82
	}
	
	void testValueFromObjectField() {
		// Compiere, MLookup.java:490
	}
	
	void testStringToString() {
		// Compiere, somewhere
	}
	
	void testOracle() throws SQLException {
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		try {
			Class.forName ("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Connection conn = DriverManager.getConnection(url, "compiere", "password");
		// @machineName:port:SID,   userid,  password

		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery("select BANNER from SYS.V_$VERSION");
		while (rset.next())
			System.out.println (rset.getString(1));   // Print col 1
		stmt.close();

	}
	
	String appendAA(String s) {
		return s + "AA";
	}
	
	void testNew(String arg) {
		String u = "a" + "b" + "c";
		String str = "aaa"; //, x = "oo";
		u = "uu";
		
		if (someCond()) {
			str = "true";
		} /*else {
			str = u;
		}*/
		
		str += "bb";
		str += "cc";
		
		while (someCond()) {
			str += "x";
		}
		
		str += arg;
		
		str = "OO";
		str = appendAA(str);
		
		System.out.println(str);
	}
	
	
	void SB() {
		StringBuilder sb = new StringBuilder("obaa");
		
		sb.append("tere");
		
		System.out.print(sb);
	}
	
	boolean someCond() {
		return true;
	}
}