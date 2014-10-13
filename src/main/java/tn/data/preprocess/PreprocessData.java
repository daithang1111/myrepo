package tn.data.preprocess;

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
import org.apache.log4j.Logger;

import tn.model.generic.DataModel;
import tn.model.recommendation.Vocabulary;
import tn.topicmodel.data.press.ProcessPressRelease;
import tn.util.Consts;
import tn.util.NEFinder;

import com.google.common.base.Joiner;

public class PreprocessData {
	private static final Logger LOG = Logger.getLogger(PreprocessData.class);
	private static final String INPUT_OPTION = "inputFile";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String STOPWORD_OPTION = "stopWord";
	private static final String MIN_OPTION = "minNumWord";
	private static final String NEFINDER_OPTION = "neFinder";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("inputFile").hasArg()
				.withDescription("inputFile name, follow datamodel format")
				.create(INPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("stopWord").hasArg()
				.withDescription("stop word file").create(STOPWORD_OPTION));

		options.addOption(OptionBuilder.withArgName("minNumWord").hasArg()
				.withDescription("minimum words per document to keep")
				.create(MIN_OPTION));

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
				|| !cmdline.hasOption(INPUT_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(ProcessPressRelease.class.getCanonicalName(),
					options);
			System.exit(-1);
		}

		String inputFile = cmdline.getOptionValue(INPUT_OPTION);
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

		int minWords = 0;
		if (cmdline.hasOption(MIN_OPTION)) {
			try {
				minWords = Integer.parseInt(cmdline.getOptionValue(MIN_OPTION));
			} catch (Exception pe) {
				pe.printStackTrace();
				LOG.info("invalid min option, use 0");
			}
		}

		// read into file
		List<String> inputTextList = Consts.readFileAsList(inputFile);
		Vocabulary vocab = new Vocabulary();
		String outputFile = outputDir + "/output.txt";
		// delete current output
		Consts.fileWriter("", outputFile, false);
		for (int i = 0; i < inputTextList.size(); i++) {
			DataModel dm = Consts.toDataModel(inputTextList.get(i));
			if (dm != null) {
				List<String> cleanWordList = Consts.cleanText(dm.getDoc()
						.getText(), stop_words, neFinder);
				if (cleanWordList != null && cleanWordList.size() >= minWords) {
					String cleanedText = Joiner.on(" ").join(cleanWordList);
					dm.getDoc().setText(cleanedText);
					Consts.fileWriter(dm.toString() + "\n", outputFile, true);
					vocab.addText(cleanedText);
				}
			}
		}

		// store dictionary for later usage
		Consts.printVocab(vocab, outputDir);
	}

}
