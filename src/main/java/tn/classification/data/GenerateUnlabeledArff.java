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

public class GenerateUnlabeledArff {
	private static final Logger LOG = Logger
			.getLogger(GenerateUnlabeledArff.class);
	private static final String INPUT_OPTION = "input";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String VOCAB_OPTION = "vocabFile";
	private static final String NEFINDER_OPTION = "neFinder";
	private static final String TXTLABELS_OPTION = "txtLabels";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("input").hasArg()
				.withDescription("input name").create(INPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("vocabFile").hasArg()
				.withDescription("stop word file").create(VOCAB_OPTION));

		options.addOption(OptionBuilder
				.withArgName("neFinder")
				.hasArg()
				.withDescription(
						"english stanford name entity reg (english.all.3class.distsim.crf.ser.gz)")
				.create(NEFINDER_OPTION));
		options.addOption(OptionBuilder.withArgName("txtLabels").hasArg()
				.withDescription("the txt file of labels")
				.create(TXTLABELS_OPTION));

		CommandLine cmdline = null;
		CommandLineParser parser = new GnuParser();
		try {
			cmdline = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Error parsing command line: "
					+ exp.getMessage());
			System.exit(-1);
		}

		if (!cmdline.hasOption(TXTLABELS_OPTION)
				|| !cmdline.hasOption(OUTPUT_OPTION)
				|| !cmdline.hasOption(INPUT_OPTION)
				|| !cmdline.hasOption(VOCAB_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(GenerateUnlabeledArff.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		LOG.info("create unlabeled arff");
		String input = cmdline.getOptionValue(INPUT_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		String vocabFile = cmdline.getOptionValue(VOCAB_OPTION);
		String txtLabels = cmdline.getOptionValue(TXTLABELS_OPTION);

		List<String> labelList = Consts.readFileAsList(txtLabels);

		List<String> word_index = Consts.readFileAsList(vocabFile);
		String word = "";
		int index = -1;
		Vocabulary vocab = new Vocabulary();
		for (int i = 0; i < word_index.size(); i++) {
			String[] values = word_index.get(i).split("\\t");
			if (values != null && values.length == 2) {
				word = values[0];
				index = Integer.parseInt(values[1]);
				vocab.getWords().put(word, index);
				vocab.getIndexes().put(index, word);
			}
		}

		String neFile = "";
		NEFinder neFinder = null;
		if (cmdline.hasOption(NEFINDER_OPTION)) {
			neFile = cmdline.getOptionValue(NEFINDER_OPTION);
			neFinder = new NEFinder(neFile);
		}

		List<String> terms = Consts.readFileAsList(input);
		List<String> cleanedTerms = new ArrayList<String>();
		for (int i = 0; i < terms.size(); i++) {
			String[] label_str = terms.get(i).split("\\t");
			if (label_str != null && label_str.length == 2) {
				List<String> clean = cleanText(label_str[1], vocab, neFinder);
				if (clean != null && clean.size() >= 10) {
					String cleanedText = Joiner.on(" ").join(clean);
					cleanedTerms.add(label_str[0] + "\t" + cleanedText);
				}
			}

		}

		// print to arff textfile

		printTextArff(cleanedTerms, outputDir, labelList);

		// print to arff frequency base

		printFrequencyArff(cleanedTerms, vocab, outputDir, labelList);

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
				+ "/unlabeled.multilabel.frequency.arff";
		String multiclassArff = outputDir
				+ "/unlabeled.multiclass.frequency.arff";

		String classForMultiClassArff = Joiner.on(",").join(labelList);

		Consts.fileWriter("@relation codebook\n\n", multilabelArff, true);
		//
		Consts.fileWriter("@relation codebook\n\n", multiclassArff, true);

		StringBuffer labels = new StringBuffer();
		for (int i = 0; i < labelList.size(); i++) {
			Consts.fileWriter("@attribute " + labelList.get(i) + " {0, 1}\n",
					multilabelArff, true);
			labels.append("?,");
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
				String str = label_str[1];
				String freqVector = printFreqVector(vocab, str);
				Consts.fileWriter(labels.toString() + freqVector + "\n",
						multilabelArff, true);
				//
				Consts.fileWriter("?," + freqVector + "\n", multiclassArff,
						true);
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
		String multilabelArff = outputDir + "/unlabeled.multilabel.text.arff";
		String multiclassArff = outputDir + "/unlabeled.multiclass.text.arff";
		String textFile = outputDir + "/unlabeled.txt";

		String classForMultiClassArff = Joiner.on(",").join(labelList);

		Consts.fileWriter("@relation codebook\n\n", multilabelArff, true);
		//
		Consts.fileWriter("@relation codebook\n\n", multiclassArff, true);

		StringBuffer labels = new StringBuffer();
		for (int i = 0; i < labelList.size(); i++) {
			Consts.fileWriter("@attribute " + labelList.get(i) + " {0, 1}\n",
					multilabelArff, true);
			labels.append("?,");
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

				Consts.fileWriter(labels.toString() + "\"" + str + "\"\n",
						multilabelArff, true);
				//
				Consts.fileWriter("?," + "\"" + str + "\"\n", multiclassArff,
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
	private static List<String> cleanText(String text, Vocabulary vocab,
			NEFinder neFinder) {
		String cleaned = text.toLowerCase().replaceAll("[\\r\\n]+", " ");
		StringTokenizer st = new StringTokenizer(cleaned);
		List<String> words = Lists.newArrayList();
		while (st.hasMoreElements()) {
			words.add((String) st.nextElement());
		}
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

		List<String> outputs = Lists.newArrayList();
		for (int i = 0; i < words.size(); i++) {
			if (vocab.getIndex(words.get(i)) != -1) {
				outputs.add(words.get(i));
			}
		}
		return outputs;
	}
}
