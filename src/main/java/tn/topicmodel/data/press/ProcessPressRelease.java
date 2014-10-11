package tn.topicmodel.data.press;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.util.Version;

import tn.recommendation.Vocabulary;
import tn.util.AnalyzerUtils;
import tn.util.Consts;
import tn.util.NEFinder;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

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

		List<String> terms = Consts.readFileAsList(pressfile);
		Vocabulary vocab = new Vocabulary();
		String cleanFile = outputDir + "/output.txt";
		for (int i = 0; i < terms.size(); i++) {
			String label_str[] = terms.get(i).split("\\t");
			if (label_str != null && label_str.length == 2) {
				List<String> clean = cleanText(label_str[1], stop_words,
						neFinder);

				if (clean != null && clean.size() >= 10) {
					String cleanedText = Joiner.on(" ").join(clean);

					Consts.fileWriter(label_str[0] + "\t" + cleanedText + "\n",
							cleanFile, true);
					vocab.addText(cleanedText);

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
		String dicFile = outputDir + "/vocab.txt";
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
	 * @return
	 */
	private static List<String> cleanText(String text, List<String> stop_words,
			NEFinder neFinder) {
		String cleaned = text.toLowerCase().replaceAll("[\\r\\n]+", " ");
		StringTokenizer st = new StringTokenizer(cleaned);
		List<String> words = Lists.newArrayList();
		while (st.hasMoreElements()) {
			words.add((String) st.nextElement());
		}
		words.removeAll(stop_words);
		cleaned = Joiner.on(" ").join(words);

		if (neFinder != null) {
			cleaned = neFinder.replaceNER(cleaned);
		}
		try {
			words = AnalyzerUtils.parse(new SimpleAnalyzer(Version.LUCENE_45),
					cleaned);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		words.removeAll(stop_words);

		return words;
	}
}
