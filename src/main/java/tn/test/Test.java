package tn.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import tn.topicmodel.algorithm.LDAGenerative;
import tn.topicmodel.algorithm.LDAGibbs;

public class Test {

	public static void main(String[] args) {
		String docTitle = "2Jan2015akaka2343.txt";
		int index = 8;
		try {
			Integer.parseInt(docTitle.substring(index, index + 1));
			index = 9;
		} catch (Exception e) {
			index = 8;
		}

		String docTime = docTitle.substring(0, index);
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMMyyyy");
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd");
		try {

			Date date = formatter.parse(docTime);
			System.out.println(formatter2.format(date));

		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
