package tn.database.sqlite;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import tn.util.Consts;

public class RunSqlScript {
	private static final Logger LOG = Logger.getLogger(RunSqlScript.class);
	private static final String DATABASE_OPTION = "database";
	private static final String SQLSCRIPT_OPTION = "sqlScript";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("sqlScript").hasArg()
				.withDescription("sql script to perform action")
				.create(SQLSCRIPT_OPTION));

		options.addOption(OptionBuilder.withArgName("database").hasArg()
				.withDescription("name of database").create(DATABASE_OPTION));

		CommandLine cmdline = null;
		CommandLineParser parser = new GnuParser();
		try {
			cmdline = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Error parsing command line: "
					+ exp.getMessage());
			System.exit(-1);
		}

		if (!cmdline.hasOption(SQLSCRIPT_OPTION)
				|| !cmdline.hasOption(DATABASE_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(RunSqlScript.class.getCanonicalName(), options);
			System.exit(-1);
		}
		String database = cmdline.getOptionValue(DATABASE_OPTION);
		String sqlScript = cmdline.getOptionValue(SQLSCRIPT_OPTION);
		LOG.info("database:" + database + ", sqlScript:" + sqlScript);

		// create table
		String script = Consts.readFile(sqlScript);
		if (script.length() > 0) {
			createTable(database, script);
		} else {
			System.err.println("Invalid Script");
			System.exit(-1);
		}
	}

	/**
	 * 
	 * @param database
	 * @param sqlScript
	 */
	private static void createTable(String database, String sqlScript) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + database);
			LOG.info("Opened database:" + database + " successfully");

			stmt = c.createStatement();

			stmt.executeUpdate(sqlScript);
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		LOG.info("Script run successfully");
	}
}
