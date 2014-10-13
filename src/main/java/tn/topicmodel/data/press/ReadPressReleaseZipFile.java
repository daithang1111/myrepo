package tn.topicmodel.data.press;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mortbay.log.Log;

import tn.util.Consts;

public class ReadPressReleaseZipFile {
	private static final String PRESS_OPTION = "pressZipFile";
	private static final String OUTPUT_OPTION = "outputDir";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("pressZipFile").hasArg()
				.withDescription("pressZipFile name").create(PRESS_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		CommandLine cmdline = null;
		CommandLineParser parser = new GnuParser();
		try {
			cmdline = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Error parsing command line: "
					+ exp.getMessage());
			System.exit(-1);
		}

		if (!cmdline.hasOption(OUTPUT_OPTION)
				|| !cmdline.hasOption(PRESS_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
					ReadPressReleaseZipFile.class.getCanonicalName(), options);
			System.exit(-1);
		}
		String pressZipFile = cmdline.getOptionValue(PRESS_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		String outputFile = outputDir + "/press.txt";

		ZipFile zipFile = null;

		try {
			zipFile = new ZipFile(pressZipFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".txt")) {
					InputStream stream = null;
					BufferedReader bf = null;
					try {
						stream = zipFile.getInputStream(entry);
						bf = new BufferedReader(new InputStreamReader(stream,
								"UTF-8"));
						String line = "";
						StringBuffer sb = new StringBuffer();
						while ((line = bf.readLine()) != null) {
							sb.append(line.replaceAll("[\\r\\n\\t]+", " ")
									+ " ");
						}
						Consts.fileWriter(
								entry.getName()
										.replace(
												"GrimmerSenatePressReleases-master\\/raw\\/",
												"")
										+ "\t" + sb.toString(), outputFile,
								true);
						bf.close();
						stream.close();
					} catch (Exception ep) {
						Log.info("error reading file:" + entry.getName());
					} finally {
						Consts.closeQuietly(bf);
						Consts.closeQuietly(stream);
					}
				}
			}
			zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Consts.closeQuietly(zipFile);
		}
	}

}
