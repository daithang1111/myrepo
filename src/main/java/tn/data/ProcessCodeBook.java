package tn.data;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
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
			stop_words = Consts.readFromFile(stopWordFile);
		}

		String neFile = "";
		NEFinder neFinder = null;
		if (cmdline.hasOption(NEFINDER_OPTION)) {
			neFile = cmdline.getOptionValue(NEFINDER_OPTION);
			neFinder = new NEFinder(neFile);
		}

		List<String> terms = Consts.readFromFile(codebook);

		// convert terms to master topic
		ArrayList<MasterTopic> masters = new ArrayList<MasterTopic>();

		MasterTopic mt = null;
		Topic tp = null;
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
				tp = new Topic();
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
				mt = new MasterTopic();
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

		// print to output
		for (int i = 0; i < masters.size(); i++) {
			Consts.fileWriter(masters.get(i).toString() + "\n", outputDir
					+ File.pathSeparator + codebook, true);
		}
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	private static List<String> cleanText(String text, List<String> stop_words,
			NEFinder neFinder) {
		String cleaned = text.replaceAll("[\\r\\n]+", " ");
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
