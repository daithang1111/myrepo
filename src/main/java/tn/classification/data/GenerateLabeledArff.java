package tn.classification.data;

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
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.util.Version;

import tn.model.generic.Vocabulary;
import tn.util.AnalyzerUtils;
import tn.util.Consts;
import tn.util.NEFinder;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class GenerateLabeledArff {
	private static final Logger LOG = Logger
			.getLogger(GenerateLabeledArff.class);
	private static final String INPUT_OPTION = "input";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String STOPWORD_OPTION = "stopWord";
	private static final String NEFINDER_OPTION = "neFinder";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("input").hasArg()
				.withDescription("input name").create(INPUT_OPTION));

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
				|| !cmdline.hasOption(INPUT_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(GenerateLabeledArff.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String input = cmdline.getOptionValue(INPUT_OPTION);
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

		List<String> terms = Consts.readFileAsList(input);
		List<String> cleanedTerms = new ArrayList<String>();
		List<String> labelList = new ArrayList<String>();
		// print out labels.xml
		// label is the topic name
		String xmlLabels = outputDir + "/labels.xml";
		String txtLabels = outputDir + "/labels.txt";
		Consts.fileWriter(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<labels xmlns=\"http://mulan.sourceforge.net/labels\">\n",
				xmlLabels, true);

		Vocabulary vocab = new Vocabulary();

		for (int i = 0; i < terms.size(); i++) {
			String label_str[] = terms.get(i).split("\\t");
			if (label_str != null && label_str.length == 2) {
				if (!labelList.contains(label_str[0])) {
					Consts.fileWriter("<label name=\"" + label_str[0]
							+ "\"></label>\n", xmlLabels, true);
					Consts.fileWriter(label_str[0] + "\n", txtLabels, true);
					labelList.add(label_str[0]);
				}
				List<String> clean = cleanText(label_str[1], stop_words,
						neFinder);

				if (clean != null && clean.size() >= 10) {
					String cleanedText = Joiner.on(" ").join(clean);

					cleanedTerms.add(label_str[0] + "\t" + cleanedText);
					vocab.addText(cleanedText);
				}
			}
		}

		Consts.fileWriter("</labels>\n", xmlLabels, true);

		// print to arff textfile
		printTextArff(cleanedTerms, outputDir, labelList);

		// print to arff frequency base
		// need to build dictionary first

		printFrequencyArff(cleanedTerms, vocab, outputDir, labelList);

		// store dictionary for later usage
		printVocab(vocab, outputDir);
	}

	/**
	 * 
	 * @param vocab
	 * @param dicFile
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
	 * @param masters
	 * @param vocab
	 * @param arffFile
	 */
	private static void printFrequencyArff(List<String> texts,
			Vocabulary vocab, String outputDir, List<String> labelList) {

		// for multilabel
		String multilabelArff = outputDir
				+ "/labeled.multilabel.frequency.arff";
		String multiclassArff = outputDir
				+ "/labeled.multiclass.frequency.arff";

		String classForMultiClassArff = Joiner.on(",").join(labelList);

		Consts.fileWriter("@relation codebook\n\n", multilabelArff, true);
		//
		Consts.fileWriter("@relation codebook\n\n", multiclassArff, true);

		for (int i = 0; i < labelList.size(); i++) {
			Consts.fileWriter("@attribute " + labelList.get(i) + " {0, 1}\n",
					multilabelArff, true);
		}
		//
		Consts.fileWriter("@attribute ZZZCLASS {" + classForMultiClassArff
				+ "}\n", multiclassArff, true);

		for (int i = 0; i < vocab.getVocabSize(); i++) {
			Consts.fileWriter("@attribute " + vocab.getWord(i + 1)
					+ " numeric\n", multilabelArff, true);
			//
			Consts.fileWriter("@attribute " + vocab.getWord(i + 1)
					+ " numeric\n", multiclassArff, true);
		}

		Consts.fileWriter("@data\n", multilabelArff, true);
		//
		Consts.fileWriter("@data\n", multiclassArff, true);

		for (int i = 0; i < texts.size(); i++) {
			String label_str[] = texts.get(i).split("\\t");
			if (label_str.length == 2) {
				String label = label_str[0];
				String str = label_str[1];
				StringBuffer tmpLabel = new StringBuffer();
				for (int j = 0; j < labelList.size(); j++) {
					if (labelList.get(j).equalsIgnoreCase(label)) {
						tmpLabel.append("1,");
					} else {
						tmpLabel.append("0,");
					}

				}

				String freqVector = printFreqVector(vocab, str);
				Consts.fileWriter(tmpLabel.toString() + freqVector + "\n",
						multilabelArff, true);

				Consts.fileWriter(label + "," + freqVector + "\n",
						multiclassArff, true);
			}
		}
	}

	/**
	 * print comma delim for frequency of this list
	 * 
	 * @param list
	 * @return
	 */
	private static String printFreqVector(Vocabulary vocab, String text) {
		StringTokenizer st = new StringTokenizer(text);
		List<String> list = Lists.newArrayList();
		while (st.hasMoreElements()) {
			list.add((String) st.nextElement());
		}
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		for (int i = 0; i < list.size(); i++) {
			String word = list.get(i);
			if (hash.containsKey(word)) {
				hash.put(word, hash.get(word) + 1);
			} else {
				hash.put(word, 1);
			}
		}

		// create init vector of 0s
		List<Integer> vector = new ArrayList<Integer>();
		for (int i = 0; i < vocab.getVocabSize(); i++) {
			vector.add(0);
		}

		// embed appropriate frequency
		Set<Entry<String, Integer>> set = hash.entrySet();
		for (Entry<String, Integer> entry : set) {
			int index = vocab.getIndex(entry.getKey());
			if (index != -1) {
				int frequency = entry.getValue();
				vector.set(index - 1, frequency);
			} else {
				LOG.info("NO WORD: " + entry.getKey());
			}
		}

		return Joiner.on(",").join(vector);
	}

	/**
	 * 
	 * @param masters
	 * @param arffFile
	 */
	private static void printTextArff(List<String> texts, String outputDir,
			List<String> labelList) {

		// for multilabel
		String multilabelArff = outputDir + "/labeled.multilabel.text.arff";
		String multiclassArff = outputDir + "/labeled.multiclass.text.arff";
		String textFile = outputDir + "/labeled.txt";

		String classForMultiClassArff = Joiner.on(",").join(labelList);

		Consts.fileWriter("@relation codebook\n\n", multilabelArff, true);
		//
		Consts.fileWriter("@relation codebook\n\n", multiclassArff, true);

		for (int i = 0; i < labelList.size(); i++) {
			Consts.fileWriter("@attribute " + labelList.get(i) + " {0, 1}\n",
					multilabelArff, true);
		}
		//
		Consts.fileWriter("@attribute ZZZCLASS {" + classForMultiClassArff
				+ "}\n", multiclassArff, true);

		Consts.fileWriter("@attribute keywords string\n\n", multilabelArff,
				true);
		//
		Consts.fileWriter("@attribute keywords string\n\n", multiclassArff,
				true);

		Consts.fileWriter("@data\n", multilabelArff, true);
		//
		Consts.fileWriter("@data\n", multiclassArff, true);

		for (int i = 0; i < texts.size(); i++) {
			String label_str[] = texts.get(i).split("\\t");
			if (label_str.length == 2) {
				String label = label_str[0];
				String str = label_str[1];
				StringBuffer tmpLabel = new StringBuffer();
				for (int j = 0; j < labelList.size(); j++) {
					if (labelList.get(j).equalsIgnoreCase(label)) {
						tmpLabel.append("1,");
					} else {
						tmpLabel.append("0,");
					}

				}
				Consts.fileWriter(tmpLabel.toString() + "\"" + str + "\"\n",
						multilabelArff, true);
				//
				Consts.fileWriter(label + ",\"" + str + "\"\n", multiclassArff,
						true);

				Consts.fileWriter(label + "\t" + str + "\n", textFile, true);
			}
		}
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
