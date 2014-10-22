package tn.data.preprocess;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
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

public class GenVocab {
	private static final Logger LOG = Logger.getLogger(GenVocab.class);
	private static final String VOCAB_OPTION = "vocabFile";
	private static final String OUTPUT_OPTION = "outputDir";

	private static final String ALGO_OPTION = "algoName";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("vocabFile").hasArg()
				.withDescription("vocab file").create(VOCAB_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("algoName").hasArg()
				.withDescription("algorithm name").create(ALGO_OPTION));

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
				|| !cmdline.hasOption(VOCAB_OPTION)
				|| !cmdline.hasOption(ALGO_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(GenVocab.class.getCanonicalName(), options);
			System.exit(-1);
		}
		String vocab = cmdline.getOptionValue(VOCAB_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		String algoName = cmdline.getOptionValue(ALGO_OPTION);
		//
		List<String> wordList = Consts.readFileAsList(vocab);

		String outputFile = outputDir + "/" + algoName + ".vocab.txt";
		for (int i = 0; i < wordList.size(); i++) {

			String index_word[] = wordList.get(i).split("\\t");
			if (index_word.length == 2) {
				Consts.fileWriter(algoName + "\t" + index_word[0] + "\t"
						+ index_word[1] + "\n", outputFile, true);

			}

		}

	}
}
