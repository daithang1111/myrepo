package tn.topicmodel.data.mrlda;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
import tn.topicmodel.data.press.ProcessPressRelease;
import tn.util.Consts;

public class GenTopicResult {
	private static final Logger LOG = Logger.getLogger(GenTopicResult.class);
	private static final String TOPIC_OPTION = "topicFile";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String DOCTOPIC_OPTION = "docTopicFile";
	private static final String CORPUS_OPTION = "corpusFile";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("topicFile").hasArg()
				.withDescription("topic, line by line").create(TOPIC_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("docTopicFile").hasArg()
				.withDescription("mrlda doc, topic file (gamma file)")
				.create(DOCTOPIC_OPTION));

		options.addOption(OptionBuilder.withArgName("corpusFile").hasArg()
				.withDescription("original corpus file").create(CORPUS_OPTION));

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
				|| !cmdline.hasOption(TOPIC_OPTION)
				|| !cmdline.hasOption(DOCTOPIC_OPTION)
				|| !cmdline.hasOption(CORPUS_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(ProcessPressRelease.class.getCanonicalName(),
					options);
			System.exit(-1);
		}

		String topicFile = cmdline.getOptionValue(TOPIC_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		String docTopicFile = cmdline.getOptionValue(DOCTOPIC_OPTION);
		String corpusFile = cmdline.getOptionValue(CORPUS_OPTION);

		String prefix = "mrlda";

		// print topics
		String outputTopic = outputDir + "/" + prefix + ".topic";
		List<String> topics = Consts.readFileAsList(topicFile);
		for (int i = 0; i < topics.size(); i++) {
			Consts.fileWriter(prefix + (i + 1) + "\t" + topics.get(i) + "\n",
					outputTopic, true);
		}

		// print topicresult
		List<String> corpusList = Consts.readFileAsList(corpusFile);
		List<String> docTopicList = Consts.readFileAsList(docTopicFile);
		if (corpusList.size() != docTopicList.size()) {
			LOG.info("Mismatch of length of corpus and doc-topic file");
			return;
		}

		String outputTopicResult = outputDir + "/" + prefix + ".topicresult";
		double prop = 0.0;
		String timestamp = "";
		String actorId = "";
		String docId = "";
		for (int i = 0; i < docTopicList.size(); i++) {
			String[] values = docTopicList.get(i).split(" ");
			int docIndex = Integer.parseInt(values[0]) - 1;
			double sumProp = 0.0;
			int topicIndex = -1;
			double bestTopicProp = 0.0;
			for (int j = 1; j < values.length; j++) {
				double tmp = Double.parseDouble(values[j]);
				sumProp += tmp;
				if (bestTopicProp < tmp) {
					topicIndex = j;
					bestTopicProp = tmp;
				}
			}
			String topicId = prefix + topicIndex;
			prop = bestTopicProp / sumProp;
			DataModel dm = Consts.toDataModel(corpusList.get(docIndex));
			timestamp = dm.getDataGroup().toString(); //TODO
			actorId = dm.getActor().getActorId();
			docId = dm.getDoc().getDocId();
			Consts.fileWriter(actorId + "\t" + timestamp + "\t" + docId + "\t"
					+ topicId + "\t" + prop + "\n", outputTopicResult, true);
		}

	}
}
