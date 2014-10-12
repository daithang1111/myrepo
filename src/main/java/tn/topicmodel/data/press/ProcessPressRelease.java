package tn.topicmodel.data.press;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import tn.model.recommendation.Vocabulary;
import tn.util.Consts;
import tn.util.NEFinder;

import com.google.common.base.Joiner;

public class ProcessPressRelease {
	private static final String PRESS_OPTION = "pressfile";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String STOPWORD_OPTION = "stopWord";
	private static final String NEFINDER_OPTION = "neFinder";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("pressfile").hasArg()
				.withDescription("pressfile name").create(PRESS_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("stopWord").hasArg()
				.withDescription("stop word file").create(STOPWORD_OPTION));

		options.addOption(OptionBuilder
				.withArgName("neFinder")
				.hasArg()
				.withDescription(
						"english stanford name entity reg (english.all.3class.distsim.crf.ser.gz)")
				.create(NEFINDER_OPTION));

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
			formatter.printHelp(ProcessPressRelease.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String pressfile = cmdline.getOptionValue(PRESS_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);

		String stopWordFile = "";
		List<String> stop_words = new ArrayList<String>();
		if (cmdline.hasOption(STOPWORD_OPTION)) {
			stopWordFile = cmdline.getOptionValue(STOPWORD_OPTION);
			stop_words = Consts.readFileAsList(stopWordFile);
		}

		String neFile = "";
		NEFinder neFinder = null;
		if (cmdline.hasOption(NEFINDER_OPTION)) {
			neFile = cmdline.getOptionValue(NEFINDER_OPTION);
			neFinder = new NEFinder(neFile);
		}

		List<String> inputTextList = Consts.readFileAsList(pressfile);
		Vocabulary vocab = new Vocabulary();
		String outputFile = outputDir + "/output.txt";
		Consts.fileWriter("", outputFile, false);
		
		for (int i = 0; i < inputTextList.size(); i++) {
			String label_str[] = inputTextList.get(i).split("\\t");
			if (label_str != null && label_str.length == 2) {
				List<String> cleanWordList = Consts.cleanText(label_str[1], stop_words,
						neFinder);

				if (cleanWordList != null && cleanWordList.size() >= 10) {
					String cleanedText = Joiner.on(" ").join(cleanWordList);

					Consts.fileWriter(label_str[0] + "\t" + cleanedText + "\n",
							outputFile, true);
					vocab.addText(cleanedText);

				}
			}
		}

		// store dictionary for later usage
		Consts.printVocab(vocab, outputDir);
	}

}
