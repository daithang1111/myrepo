package tn.classification.data.codebook;

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

import tn.model.codebook.MasterTopicCode;
import tn.model.codebook.TopicCode;
import tn.recommendation.Vocabulary;
import tn.util.AnalyzerUtils;
import tn.util.Consts;
import tn.util.NEFinder;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ProcessCodeBook {
	private static final Logger LOG = Logger.getLogger(ProcessCodeBook.class);
	private static final String CODEBOOK_OPTION = "codebook";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String STOPWORD_OPTION = "stopWord";
	private static final String NEFINDER_OPTION = "neFinder";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("codebook").hasArg()
				.withDescription("codebook name").create(CODEBOOK_OPTION));

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
				|| !cmdline.hasOption(CODEBOOK_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(ProcessCodeBook.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String codebook = cmdline.getOptionValue(CODEBOOK_OPTION);
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

		List<String> terms = Consts.readFileAsList(codebook);

		// convert terms to master topic
		ArrayList<MasterTopicCode> masters = new ArrayList<MasterTopicCode>();

		MasterTopicCode mt = null;
		TopicCode tp = null;
		for (int i = 0; i < terms.size(); i++) {
			String line = terms.get(i);
			if (line.startsWith("See also:")) {
				LOG.info("Skip:" + line);
			} else if (line.startsWith("Examples:")) {
				// process example
				int index = line.indexOf(":");
				List<String> examples = cleanText(line.substring(index + 2),
						stop_words, neFinder);
				tp.setKeywords(examples);

				// add topic to master topic
				mt.getTopics().add(tp);
			} else if (line.contains(":")) {
				// process topic
				tp = new TopicCode();
				int index = line.indexOf(":");
				if (index != -1) {
					try {
						tp.setCode(Integer.parseInt(line.substring(0, index)));
						tp.setName(line.substring(index + 2));

						// decide what type
						if (tp.getCode() % 100 == 0) {
							tp.setType("general");
						} else {
							tp.setType("specific");
						}
					} catch (Exception e) {
						LOG.info("Skip:" + line);
					}
				}
			} else {
				// this must be a master topic
				if (mt != null) {
					masters.add(mt);
				}
				mt = new MasterTopicCode();
				tp = new TopicCode();
				int index = line.indexOf(".");
				if (index != -1) {
					try {
						mt.setCode(Integer.parseInt(line.substring(0, index)));
						mt.setName(line.substring(index + 2));
					} catch (Exception e) {
						LOG.info("Skip:" + line);
					}
				}
			}

		}
		masters.add(mt);

		/**
		 * There are two types of arff files, 1 for multi-label and 1 for
		 * multi-class classifiers The focus will be on multi-label, but
		 * multi-class will always be supported
		 */
		// print out labels.xml
		// label is the topic name
		String xmlLabels = outputDir + "/labels.xml";
		String txtLabels = outputDir + "/labels.txt";
		List<String> labelList = new ArrayList<String>();
		for (int i = 0; i < masters.size(); i++) {
			labelList.add(masters.get(i).getName()
					.replaceAll("[^a-zA-Z0-9]", ""));
		}

		Consts.fileWriter(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<labels xmlns=\"http://mulan.sourceforge.net/labels\">\n",
				xmlLabels, true);

		for (int i = 0; i < labelList.size(); i++) {
			Consts.fileWriter("<label name=\"" + labelList.get(i)
					+ "\"></label>\n", xmlLabels, true);
			Consts.fileWriter(labelList.get(i) + "\n", txtLabels, true);
		}

		Consts.fileWriter("</labels>\n", xmlLabels, true);

		// print to arff textfile
		printTextArff(masters, outputDir, labelList);

		// print to arff frequency base
		// need to build dictionary first

		Vocabulary vocab = new Vocabulary();
		for (int i = 0; i < masters.size(); i++) {
			List<TopicCode> topics = masters.get(i).getTopics();
			for (TopicCode topic : topics) {
				vocab.addText(Joiner.on(" ").join(topic.getKeywords()));
			}
		}

		printFrequencyArff(masters, vocab, outputDir, labelList);

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
	 * @param masters
	 * @param vocab
	 * @param arffFile
	 */
	private static void printFrequencyArff(List<MasterTopicCode> masters,
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
		Consts.fileWriter(
				"@attribute ZZZCLASS {" + classForMultiClassArff + "}\n",
				multiclassArff, true);

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

		for (int i = 0; i < masters.size(); i++) {
			StringBuffer labels = new StringBuffer();
			for (int j = 0; j < i; j++) {
				labels.append("0,");
			}
			labels.append("1,");
			for (int j = i + 1; j < masters.size(); j++) {
				labels.append("0,");
			}

			MasterTopicCode master = masters.get(i);

			List<TopicCode> topics = master.getTopics();
			for (int j = 0; j < topics.size(); j++) {
				String freqVector = printFreqVector(vocab, topics.get(j)
						.getKeywords());

				Consts.fileWriter(labels.toString() + freqVector + "\n",
						multilabelArff, true);
				//
				Consts.fileWriter(labelList.get(i) + "," + freqVector + "\n",
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
	private static String printFreqVector(Vocabulary vocab, List<String> list) {
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
	 * @param outputDir
	 * @param labelList
	 */
	private static void printTextArff(List<MasterTopicCode> masters,
			String outputDir, List<String> labelList) {

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
		Consts.fileWriter(
				"@attribute ZZZCLASS {" + classForMultiClassArff + "}\n",
				multiclassArff, true);

		Consts.fileWriter("@attribute keywords string\n\n", multilabelArff,
				true);
		//
		Consts.fileWriter("@attribute keywords string\n\n", multiclassArff,
				true);

		Consts.fileWriter("@data\n", multilabelArff, true);
		//
		Consts.fileWriter("@data\n", multiclassArff, true);
		for (int i = 0; i < masters.size(); i++) {
			StringBuffer labels = new StringBuffer();
			for (int j = 0; j < i; j++) {
				labels.append("0,");
			}
			labels.append("1,");
			for (int j = i + 1; j < masters.size(); j++) {
				labels.append("0,");
			}

			MasterTopicCode master = masters.get(i);

			List<TopicCode> topics = master.getTopics();
			for (int j = 0; j < topics.size(); j++) {
				String text = Joiner.on(" ").join(topics.get(j).getKeywords());
				Consts.fileWriter(labels.toString() + "\"" + text + "\"\n",
						multilabelArff, true);
				//
				Consts.fileWriter(labelList.get(i) + ",\"" + text + "\"\n",
						multiclassArff, true);

				Consts.fileWriter(labelList.get(i) + "\t" + text + "\n",
						textFile, true);
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
