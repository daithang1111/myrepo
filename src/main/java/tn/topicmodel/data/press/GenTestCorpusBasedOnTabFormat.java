package tn.topicmodel.data.press;

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

import tn.model.generic.Actor;
import tn.model.generic.DataGroup;
import tn.model.generic.DataModel;
import tn.model.generic.Document;
import tn.util.Consts;

public class GenTestCorpusBasedOnTabFormat {
	private static final String INPUT_OPTION = "inputFile";
	private static final String OUTPUT_OPTION = "outputDir";

	private static final String CORPUSNAME_OPTION = "corpusName";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("inputFile").hasArg()
				.withDescription("inputFile name").create(INPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("corpusName").hasArg()
				.withDescription("name for this corpus")
				.create(CORPUSNAME_OPTION));
		
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
				|| !cmdline.hasOption(INPUT_OPTION)|| !cmdline.hasOption(CORPUSNAME_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
					GenTestCorpusBasedOnTabFormat.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String inputFile = cmdline.getOptionValue(INPUT_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		String corpusName =cmdline.getOptionValue(CORPUSNAME_OPTION);

		List<String> inputTextList = Consts.readFileAsList(inputFile);
		String outputFile = outputDir + "/"+corpusName+".corpus";
		String errorFile = outputDir + "error.txt";
		Consts.fileWriter("", outputFile, false);

		for (int i = 0; i < inputTextList.size(); i++) {
			String label_str[] = inputTextList.get(i).toLowerCase()
					.split("\\t");
			if (label_str != null && label_str.length == 2) {
				String docFileName = label_str[0];
				String docContent = label_str[1];

				// we need to create several entries
				// GroupId, description, actorId, actorName, docId, docTitle,
				// docTime, docContent
				// these steps are overhead, but just to express a concept
					// of data model
					String actorId = corpusName;
					String actorName = actorId;
					Actor actor = new Actor();
					actor.setActorId(actorId);
					actor.setActorName(actorName);

					String docId = docFileName;
					String docTitle = docId;

					String groupId = corpusName;
					String desc = groupId;
					String docTime = Consts.getTIME_CONST();

					DataGroup dg = new DataGroup();
					dg.setDescription(desc);
					dg.setGroupId(groupId);

					Document doc = new Document(docId, docTitle, docTime,
							docContent);

					DataModel dm = new DataModel();
					dm.setActor(actor);
					dm.setDataGroup(dg);
					dm.setDoc(doc);

					Consts.fileWriter(dm.toString() + "\n", outputFile, true);

			} else {
				Consts.fileWriter(inputTextList.get(i) + "\n", errorFile, true);
			}
		}

	}


}
