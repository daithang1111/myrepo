package tn.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import tn.topicmodel.algorithm.LDAGenerative;
import tn.topicmodel.algorithm.LDAGibbs;

public class Test {

	public static void main(String[] args) {
//		LDAGenerative ldaGen = new LDAGenerative(10, 20, 50, 10, 0.01, 0.5);
//		ldaGen.generate();
		// print sample docs
//		System.out.println("Before sampling:");
//		ldaGen.print();

//		LDAGibbs ldaGibbs = new LDAGibbs(ldaGen.getM(), ldaGen.getV(),
//				ldaGen.getK(), ldaGen.getDocs(), ldaGen.getAlpha(),
//				ldaGen.getBeta(), 20000, 100);
//		
//		ldaGibbs.run();
		//print draw
//		ldaGen.setZ(ldaGibbs.getZ());

		// System.out.println("After sampling:");
		// ldaGen.print();
		
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
