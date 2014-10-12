package tn.topicmodel.data.press;

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

import tn.model.recommendation.Vocabulary;
import tn.util.Consts;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class TruncatePressRelease {
	private static final String PRESS_OPTION = "pressfile";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String MIN_OPTION = "minDocFreq";
	private static final String MAX_OPTION = "maxDocFreq";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("pressfile").hasArg()
				.withDescription("pressfile name").create(PRESS_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("minDocFreq").hasArg()
				.withDescription("min doc frequency for word")
				.create(MIN_OPTION));

		options.addOption(OptionBuilder.withArgName("maxDocFreq").hasArg()
				.withDescription("max doc frequency for word")
				.create(MAX_OPTION));

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
				|| !cmdline.hasOption(PRESS_OPTION)
				|| !cmdline.hasOption(MIN_OPTION)
				|| !cmdline.hasOption(MAX_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(TruncatePressRelease.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String pressfile = cmdline.getOptionValue(PRESS_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		int minDocFreq = Integer.parseInt(cmdline.getOptionValue(MIN_OPTION));
		int maxDocFreq = Integer.parseInt(cmdline.getOptionValue(MAX_OPTION));

		List<String> terms = Consts.readFileAsList(pressfile);
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		for (int i = 0; i < terms.size(); i++) {
			String label_str[] = terms.get(i).split("\\t");
			if (label_str != null && label_str.length == 2) {
				List<String> uniqWords = getUniqWords(label_str[1]);
				for (String word : uniqWords) {
					if (hash.containsKey(word)) {
						hash.put(word, hash.get(word) + 1);
					} else {
						hash.put(word, 1);
					}
				}
			}
		}

		Vocabulary vocab = new Vocabulary();
		Set<Entry<String, Integer>> set = hash.entrySet();
		for (Entry<String, Integer> entry : set) {
			int freq = entry.getValue();
			if (freq >= minDocFreq && freq <= maxDocFreq) {
				vocab.addText(entry.getKey());
			}
		}

		// print clean text
		String cleanFile = outputDir + "/output.txt.trunc";
		for (int i = 0; i < terms.size(); i++) {
			String label_str[] = terms.get(i).split("\\t");
			if (label_str != null && label_str.length == 2) {
				List<String> newText = filterText(label_str[1], vocab);
				if (newText != null && newText.size() >= 10) {

					Consts.fileWriter(label_str[0] + "\t"
							+ Joiner.on(" ").join(newText) + "\n", cleanFile,
							true);
				}
			}

		}
		// store dictionary for later usage
		printVocab(vocab, outputDir);
	}

	/**
	 * 
	 * @param vocab
	 * @param outputDir
	 */
	private static void printVocab(Vocabulary vocab, String outputDir) {
		String dicFile = outputDir + "/vocab.txt.trunc";
		Set<Entry<String, Integer>> set = vocab.getWords().entrySet();
		List<String> list = new ArrayList<String>();
		for (Entry<String, Integer> entry : set) {
			list.add(entry.getKey() + "\t" + entry.getValue());
		}
		Consts.fileWriter(Joiner.on("\n").join(list), dicFile, false);
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
