package tn.topicmodel.data.press;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class GenTestCorpusBasedOnPressRelease {
	private static final String PRESS_OPTION = "pressfile";
	private static final String OUTPUT_OPTION = "outputDir";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("pressfile").hasArg()
				.withDescription("pressfile name").create(PRESS_OPTION));

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
				|| !cmdline.hasOption(PRESS_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
					GenTestCorpusBasedOnPressRelease.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String pressfile = cmdline.getOptionValue(PRESS_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);

		List<String> inputTextList = Consts.readFileAsList(pressfile);
		String outputFile = outputDir + "/press.corpus";
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
				String values[] = docFileName.split("/");
				if (values != null && values.length == 2) {

					// these steps are overhead, but just to express a concept
					// of data model
					String actorId = values[0];
					String actorName = actorId;
					Actor actor = new Actor();
					actor.setActorId(actorId);
					actor.setActorName(actorName);

					String docId = values[1];
					String docTitle = docId;

					String groupId = createGroupId(docTitle);
					String desc = groupId;
					String docTime = createDocTime(docTitle);

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
					Consts.fileWriter(inputTextList.get(i) + "\n", errorFile,
							true);
				}

			} else {
				Consts.fileWriter(inputTextList.get(i) + "\n", errorFile, true);
			}
		}

	}

	// the format of docTitle is 22Jan2007akaka232.txt

	private static String createDocTime(String docTitle) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

		int index = 8;
		try {
			Integer.parseInt(docTitle.substring(index, index + 1));
			index = 9;
		} catch (Exception e) {
			index = 8;
		}

		String docTime = docTitle.substring(0, index);

		try {

			Date date = formatter.parse(docTime);
			return Consts.defaultFormatter.format(date);

		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Consts.getTIME_CONST();
	}

	private static String createGroupId(String docTitle) {
		return "050607";
	}

}
