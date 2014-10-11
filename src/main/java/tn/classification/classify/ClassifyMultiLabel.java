package tn.classification.classify;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import mulan.classifier.lazy.MLkNN;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluator;
import mulan.evaluation.MultipleEvaluation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import tn.util.Consts;
import weka.classifiers.trees.J48;

public class ClassifyMultiLabel {
	private static final Logger LOG = Logger
			.getLogger(ClassifyMultiLabel.class);
	private static final String ARFFTRAIN_OPTION = "arffTrain";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String XMLLABELS_OPTION = "xmlLabels";
	private static final String NUMFOLD_OPTION = "numFolds";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("arffTrain").hasArg()
				.withDescription("arff training file").create(ARFFTRAIN_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir").create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("xmlLabels").hasArg()
				.withDescription("label xml file").create(XMLLABELS_OPTION));

		options.addOption(OptionBuilder.withArgName("numFolds").hasArg()
				.withDescription("number of fold for cross validation")
				.create(NUMFOLD_OPTION));

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
				|| !cmdline.hasOption(ARFFTRAIN_OPTION)
				|| !cmdline.hasOption(XMLLABELS_OPTION)
				|| !cmdline.hasOption(NUMFOLD_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(ClassifyMultiLabel.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String arffTrain = cmdline.getOptionValue(ARFFTRAIN_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		String xmlLabels = cmdline.getOptionValue(XMLLABELS_OPTION);
		int numFolds = Integer.parseInt(cmdline.getOptionValue(NUMFOLD_OPTION));
		try {
			LOG.info("Traing 2 learners on dataset");
			// get training data
			MultiLabelInstances dataset = new MultiLabelInstances(arffTrain,
					xmlLabels);

			// 2 meta classifiers
			RAkEL learner1 = new RAkEL(new LabelPowerset(new J48()));
			MLkNN learner2 = new MLkNN();

			// evaluation tool
			Evaluator eval = new Evaluator();
			MultipleEvaluation results;

			results = eval.crossValidate(learner1, dataset, numFolds);
			String output = outputDir + "/learner1.out";
			Consts.fileWriter(results.toCSV(), output, true);

			output = outputDir + "/learner2.out";

			results = eval.crossValidate(learner2, dataset, numFolds);
			Consts.fileWriter(results.toCSV(), output, true);

		} catch (InvalidDataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
