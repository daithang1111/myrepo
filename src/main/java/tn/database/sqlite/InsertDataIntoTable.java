package tn.database.sqlite;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import tn.util.Consts;

public class InsertDataIntoTable {
	private static final Logger LOG = Logger
			.getLogger(InsertDataIntoTable.class);
	private static final String DATABASE_OPTION = "database";
	private static final String SQLSCRIPT_OPTION = "insertScript";
	private static final String DATA_OPTION = "dataDir";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("insertScript").hasArg()
				.withDescription("sql script to perform action")
				.create(SQLSCRIPT_OPTION));

		options.addOption(OptionBuilder.withArgName("dataDir").hasArg()
				.withDescription("a directory to store data (bar | delim)")
				.create(DATA_OPTION));

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

		if (!cmdline.hasOption(DATA_OPTION)
				|| !cmdline.hasOption(SQLSCRIPT_OPTION)
				|| !cmdline.hasOption(DATABASE_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(InsertDataIntoTable.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String database = cmdline.getOptionValue(DATABASE_OPTION);
		String insertScript = cmdline.getOptionValue(SQLSCRIPT_OPTION);
		String dataDir = cmdline.getOptionValue(DATA_OPTION);
		LOG.info("database:" + database + ", insertScript:" + insertScript
				+ ", dataDir:" + dataDir);

		// load into database
		String script = Consts.readFile(insertScript);
		if (script.length() > 0) {
			final File dataFolder = new File(dataDir);
			for (final File fileEntry : dataFolder.listFiles()) {
				if (!fileEntry.isDirectory()) {
					String fileName = fileEntry.getPath();
					insertIntoTable(database, script, fileName);
				}
			}
		} else {
			System.err.println("Invalid Script");
			System.exit(-1);
		}
	}

	/**
	 * 
	 * @param database
	 * @param insertScript
	 * @param dataFile
	 */
	private static void insertIntoTable(String database, String insertScript,
			String dataFile) {
		
		List<String> data = Consts.readFileAsList(dataFile);
		if (data != null && data.size() > 0) {
			Connection c = null;
			try {
				Class.forName("org.sqlite.JDBC");
				c = DriverManager.getConnection("jdbc:sqlite:" + database);
				c.setAutoCommit(false);
				System.out.println("Opened database successfully");
				PreparedStatement ps = c.prepareStatement(insertScript);

				int MAX_INSERT = 1000;// insert 1000 rows at a time
				for (int i = 0; i < data.size(); i++) {
					String[] values = data.get(i).split("\\t");
					for (int j = 0; j < values.length; j++) {
						ps.setString((j + 1), values[j]);
					}
					ps.addBatch();
					if (i > 0 && i % MAX_INSERT == 0) {
						ps.executeBatch();
						LOG.info("Inserted record:" + (i + 1));
					}
				}
				ps.executeBatch();
				c.commit();
			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
				System.exit(0);
			} finally {
				try {
					c.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			LOG.info("Insert successfully");

		}

	}
}
