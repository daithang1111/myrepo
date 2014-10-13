package tn.topicmodel.data.mrlda;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import tn.topicmodel.data.press.ProcessPressRelease;
import tn.util.Consts;

public class ConvertMrldaTopicToTopic {
	private static final Logger LOG = Logger
			.getLogger(ConvertMrldaTopicToTopic.class);
	private static final String INPUT_OPTION = "mrldaTopic";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String TOPWORD_OPTION = "topWord";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("mrldaTopic").hasArg()
				.withDescription("the topic output from mrlda")
				.create(INPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("topWord").hasArg()
				.withDescription("num of top words").create(TOPWORD_OPTION));

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
				|| !cmdline.hasOption(INPUT_OPTION)
				|| !cmdline.hasOption(TOPWORD_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(ProcessPressRelease.class.getCanonicalName(),
					options);
			System.exit(-1);
		}

		String inputFile = cmdline.getOptionValue(INPUT_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		int topWord = -1;
		try {
			topWord = Integer.parseInt(cmdline.getOptionValue(TOPWORD_OPTION));
		} catch (Exception pe) {
			pe.printStackTrace();
			LOG.info("invalid num top words");
			return;
		}

		// convert data
		String outputFile = outputDir + "/topic.txt";

		List<String> mrldaTopic = Consts.readFileAsList(inputFile);
		int streak = topWord + 3;
		for (int i = 0; i < mrldaTopic.size(); i++) {
			if (i % streak > 2) {
				if (i % streak == streak - 1) {
					Consts.fileWriter(mrldaTopic.get(i).split(" ")[0] + "\n",
							outputFile, true);
				} else {
					Consts.fileWriter(mrldaTopic.get(i).split(" ")[0] + " ",
							outputFile, true);
				}
			}
		}
	}
}
