package tn.classification.classify;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import mulan.data.InvalidDataFormatException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import tn.util.Consts;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class PredictMultiClassTree {
	private static final Logger LOG = Logger.getLogger(PredictMultiClassTree.class);
	private static final String ARFFTRAIN_OPTION = "arffTrain";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String UNLABELED_OPTION = "unlabeled";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("arffTrain").hasArg()
				.withDescription("arff training file").create(ARFFTRAIN_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir").create(OUTPUT_OPTION));

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
				|| !cmdline.hasOption(UNLABELED_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(PredictMultiClassTree.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String arffTrain = cmdline.getOptionValue(ARFFTRAIN_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		String arffUnlabeled = cmdline.getOptionValue(UNLABELED_OPTION);
		try {
			LOG.info("traing and do prediction");

			// load data
			DataSource source = new DataSource(arffTrain);
			Instances trainInstances = source.getDataSet();
			// setting class attribute if the data format does not provide this
			// information
			// For example, the XRFF format saves the class attribute
			// information as
			// well
			if (trainInstances.classIndex() == -1)
				trainInstances.setClassIndex(0);

			source = new DataSource(arffUnlabeled);
			Instances testInstances = source.getDataSet();
			// setting class attribute if the data format does not provide this
			// information
			// For example, the XRFF format saves the class attribute
			// information as
			// well
			if (testInstances.classIndex() == -1)
				testInstances.setClassIndex(0);

			// List<String> algOptions = Consts.readFromFile(algorithmOption);
			// if (algOptions.size() == 0) {
			// return;
			// }
			// String[] algorithm_option = algOptions.get(0).split("\\t");
			// String algorithm = algorithm_option[0];
			// String option = "";
			//
			// if (algorithm_option.length == 2) {
			// option = algorithm_option[1];
			// }
			// String[] inputOptions = weka.core.Utils.splitOptions(option);

			try {
				// filter
				// AttributeSelection as = new AttributeSelection();
				// InfoGainAttributeEval infoGain = new InfoGainAttributeEval();
				// Ranker ranker = new Ranker();
				// ranker.setThreshold(-1.7976931348623157E308);
				// ranker.setNumToSelect(100);
				// as.setEvaluator(infoGain);
				// as.setSearch(ranker);
				// model.setFilter(as);

				/**
				 * J48
				 */
				J48 tree = new J48();
				tree.buildClassifier(trainInstances);

				// NaiveBayes nb = new NaiveBayes();
				// nb.buildClassifier(trainInstances);
				// use
				// Evaluation eval_train = new Evaluation(newTest);
				// eval_train.evaluateModel(model, newTest);

				// make prediction
				String predictionOutput = outputDir + "/class.predictions.out";

				for (int i = 0; i < testInstances.numInstances(); i++) {
					double j48Pred = tree.classifyInstance(testInstances
							.instance(i));

					// double nbPred = nb.classifyInstance(testInstances
					// .instance(i));

					Consts.fileWriter(
							testInstances.classAttribute().value(
									(int) testInstances.instance(i)
											.classValue()), predictionOutput,
							true);
					Consts.fileWriter(
							"\t"
									+ testInstances.classAttribute().value(
											(int) j48Pred) + "\n",
							predictionOutput, true);

					// Consts.fileWriter("\t"
					// + testInstances.classAttribute()
					// .value((int) nbPred) + "\n",
					// predictionOutput, true);

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (InvalidDataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
