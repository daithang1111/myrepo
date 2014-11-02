package tn.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Calendar;

public class Test {

	public static void main(String[] args) {
		String abc ="this#is#nice";
		System.out.println(abc.split("#").length);
		
		long ltime = 1074912436;
		
		Calendar cal =Calendar.getInstance();
		cal.setTimeInMillis(1074912436000L);
		
		System.out.println(cal.getTime().toString());
		
		
		
		
		
		
		String x = "this\nis \tfun";
		String values = x.replaceAll("[\\r\\n\\t]+", " ");
		System.out.println(values);

	}

	public static void createTable(String database, String sqlScript) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + database);

			stmt = c.createStatement();

			stmt.executeUpdate(sqlScript);
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

}
