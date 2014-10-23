package tn.classification.data.codebook;

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

public class GenCodeBookTopicWord {
	private static final Logger LOG = Logger
			.getLogger(GenCodeBookTopicWord.class);
	private static final String CODEBOOK_OPTION = "input";
	private static final String OUTPUT_OPTION = "outputDir";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("input").hasArg()
				.withDescription("input name (labeled.txt)")
				.create(CODEBOOK_OPTION));

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
				|| !cmdline.hasOption(CODEBOOK_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(GenCodeBookTopicWord.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String codebook = cmdline.getOptionValue(CODEBOOK_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);

		//
		List<String> codeList = Consts.readFileAsList(codebook);
		HashMap<String, HashMap<String, Integer>> hash = new HashMap<String, HashMap<String, Integer>>();

		for (int i = 0; i < codeList.size(); i++) {
			String values[] = codeList.get(i).split("\\t");
			if (values.length == 2) {
				String topicId = values[0];
				String words[] = values[1].split(" ");

				HashMap<String, Integer> tmpHash = null;

				if (hash.containsKey(topicId)) {
					tmpHash = hash.get(topicId);

				} else {
					tmpHash = new HashMap<String, Integer>();
				}
				for (int j = 0; j < words.length; j++) {
					if (tmpHash.containsKey(words[j])) {
						tmpHash.put(words[j], tmpHash.get(words[j]) + 1);
					} else {

						tmpHash.put(words[j], 1);
					}
				}
				hash.put(topicId, tmpHash);

			}

		}

		String outputFile = outputDir + "/codebook.topicword";
		for (String tID : hash.keySet()) {
			HashMap<String, Integer> wordHash = hash.get(tID);
			for (String w : wordHash.keySet()) {
				Consts.fileWriter(tID + "\t" + w + "\t" + wordHash.get(w)
						+ "\n", outputFile, true);

			}

		}

		//

	}
}
