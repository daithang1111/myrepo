package tn.data.preprocess;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

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
import tn.util.Consts;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class TruncateByDocumentFrequency {
	private static final Logger LOG = Logger
			.getLogger(TruncateByDocumentFrequency.class);
	private static final String INPUT_OPTION = "inputFile";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String MIN_OPTION = "minDocFreq";
	private static final String MAX_OPTION = "maxDocFreq";
	private static final String MIN_WORD_OPTION = "minNumWord";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("inputFile").hasArg()
				.withDescription("inputFile name").create(INPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("minDocFreq").hasArg()
				.withDescription("min doc frequency for word")
				.create(MIN_OPTION));

		options.addOption(OptionBuilder.withArgName("maxDocFreq").hasArg()
				.withDescription("max doc frequency for word")
				.create(MAX_OPTION));
		options.addOption(OptionBuilder.withArgName("minNumWord").hasArg()
				.withDescription("minimum words per document to keep")
				.create(MIN_WORD_OPTION));

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
				|| !cmdline.hasOption(MIN_OPTION)
				|| !cmdline.hasOption(MAX_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
					TruncateByDocumentFrequency.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String inputFile = cmdline.getOptionValue(INPUT_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		int minDocFreq = Integer.MIN_VALUE;

		try {
			minDocFreq = Integer.parseInt(cmdline.getOptionValue(MIN_OPTION));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("invalid min doc freq, use -inft");
		}
		int maxDocFreq = Integer.MAX_VALUE;
		try {
			Integer.parseInt(cmdline.getOptionValue(MAX_OPTION));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("invalid max doc freq, use +inft");
		}

		int minWords = 0;
		if (cmdline.hasOption(MIN_WORD_OPTION)) {
			try {
				minWords = Integer.parseInt(cmdline
						.getOptionValue(MIN_WORD_OPTION));
			} catch (Exception pe) {
				pe.printStackTrace();
				LOG.info("invalid min option, use 0");
			}
		}

		List<String> inputTextList = Consts.readFileAsList(inputFile);
		Hashtable<String, Integer> wordToFreq = new Hashtable<String, Integer>();
		List<DataModel> dmList = new ArrayList<DataModel>();
		for (int i = 0; i < inputTextList.size(); i++) {
			DataModel dm = Consts.toDataModel(inputTextList.get(i));
			if (dm != null) {
				List<String> uniqWords = getUniqWords(dm.getDoc().getText());
				for (String word : uniqWords) {
					if (wordToFreq.containsKey(word)) {
						wordToFreq.put(word, wordToFreq.get(word) + 1);
					} else {
						wordToFreq.put(word, 1);
					}
				}
				dmList.add(dm);

			}

		}

		// a new vocabulary for truncated data
		Vocabulary vocab = new Vocabulary();
		Set<Entry<String, Integer>> set = wordToFreq.entrySet();
		for (Entry<String, Integer> entry : set) {
			int freq = entry.getValue();
			if (freq >= minDocFreq && freq <= maxDocFreq) {
				vocab.addText(entry.getKey());
			}
		}

		// print
		String cleanFile = outputDir + "/output.txt";
		for (int i = 0; i < dmList.size(); i++) {
			DataModel dm = dmList.get(i);
			List<String> newText = filterText(dm.getDoc().getText(), vocab);
			if (newText != null && newText.size() >= minWords) {
				dm.getDoc().setText(Joiner.on(" ").join(newText));
				Consts.fileWriter(dm.toString() + "\n", cleanFile, true);
			}

		}
		// store dictionary for later usage
		Consts.printVocab(vocab, outputDir);
	}

	/**
	 * 
	 * @param text
	 * @param vocab
	 * @return
	 */
	private static List<String> filterText(String text, Vocabulary vocab) {
		StringTokenizer st = new StringTokenizer(text);
		List<String> words = Lists.newArrayList();
		while (st.hasMoreElements()) {
			String word = (String) st.nextElement();
			if (vocab.getIndex(word) != -1) {
				words.add(word);
			}
		}
		return words;
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	private static List<String> getUniqWords(String text) {
		StringTokenizer st = new StringTokenizer(text);
		List<String> words = Lists.newArrayList();
		while (st.hasMoreElements()) {
			String word = (String) st.nextElement();
			if (!words.contains(word)) {
				words.add(word);
			}
		}
		return words;
	}
}
