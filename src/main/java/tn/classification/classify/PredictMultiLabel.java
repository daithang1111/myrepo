package tn.classification.classify;

import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import mulan.classifier.MultiLabelOutput;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
import mulan.data.MultiLabelInstances;

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
import weka.core.Instance;
import weka.core.Instances;

public class PredictMultiLabel {
	private static final Logger LOG = Logger.getLogger(PredictMultiLabel.class);
	private static final String ARFFTRAIN_OPTION = "arffTrain";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String XMLLABELS_OPTION = "xmlLabels";
	private static final String UNLABELED_OPTION = "unlabeled";

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

		options.addOption(OptionBuilder.withArgName("unlabeled").hasArg()
				.withDescription("unlabeled arff file")
				.create(UNLABELED_OPTION));

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
				|| !cmdline.hasOption(UNLABELED_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(PredictMultiLabel.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String arffTrain = cmdline.getOptionValue(ARFFTRAIN_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		String xmlLabels = cmdline.getOptionValue(XMLLABELS_OPTION);
		String arffUnlabeled = cmdline.getOptionValue(UNLABELED_OPTION);
		try {
			LOG.info("traing and do prediction");
			// get training data
			MultiLabelInstances dataset = new MultiLabelInstances(arffTrain,
					xmlLabels);

			// 2 meta classifiers
			RAkEL model = new RAkEL(new LabelPowerset(new J48()));
			try {
				model.build(dataset);

				// load unlabeled data
				FileReader reader = new FileReader(arffUnlabeled);
				Instances unlabeledData = new Instances(reader);

				// make prediction
				String predictionOutput = outputDir + "/label.predictions.out";
				int numInstances = unlabeledData.numInstances();
				for (int instanceIndex = 0; instanceIndex < numInstances; instanceIndex++) {
					Instance instance = unlabeledData.instance(instanceIndex);
					MultiLabelOutput output = model.makePrediction(instance);
					// do necessary operations with provided prediction output,
					// here just print it out
					Consts.fileWriter(output.toString() + "\n",
							predictionOutput, true);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (InvalidDataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
