package tn.classification.classify;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import jnisvmlight.LabeledFeatureVector;
import jnisvmlight.SVMLightInterface;
import jnisvmlight.SVMLightModel;
import jnisvmlight.TrainingParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import tn.util.Consts;

public class PredictMultiClassSVMLight {
	private static final Logger LOG = Logger
			.getLogger(PredictMultiClassSMO.class);
	private static final String ARFFTRAIN_OPTION = "arffTrain";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String UNLABELED_OPTION = "unlabeled";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("arffTrain").hasArg()
				.withDescription("svm light training file")
				.create(ARFFTRAIN_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir").create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("unlabeled").hasArg()
				.withDescription("unlabeled svm light file")
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
			formatter.printHelp(PredictMultiClassSMO.class.getCanonicalName(),
					options);
			System.exit(-1);
		}
		String arffTrain = cmdline.getOptionValue(ARFFTRAIN_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		String arffUnlabeled = cmdline.getOptionValue(UNLABELED_OPTION);
		try {
			LOG.info("traing and do prediction");

			// The trainer interface with the native communication to the
			// SVM-light shared
			// libraries
			SVMLightInterface trainer = new SVMLightInterface();

			// The training data
			LabeledFeatureVector[] traindata = Consts
					.readSVMLightInput(arffTrain);

			// Sort all feature vectors in ascedending order of feature
			// dimensions
			// before training the model
			SVMLightInterface.SORT_INPUT_VECTORS = true;

			// Initialize a new TrainingParamteres object with the default
			// SVM-light
			// values
			TrainingParameters tp = new TrainingParameters();

			// Switch on some debugging output
			tp.getLearningParameters().verbosity = 1;

			System.out.println("\nTRAINING SVM-light MODEL ..");
			SVMLightModel model = trainer.trainModel(traindata, tp);
			System.out.println(" DONE.");

			// Use this to store a model to a file or read a model from a URL.
			// model.writeModelToFile(outputDir+"/jni_model.dat");
			// model = SVMLightModel.readSVMLightModelFromURL(new
			// java.io.File("jni_model.dat").toURL());

			// Use the classifier on the randomly created feature vectors
			// System.out.println("\nVALIDATING SVM-light MODEL in Java..");
			// int precision = 0;

			// read test data
			LabeledFeatureVector[] testdata = Consts
					.readSVMLightInput(arffUnlabeled);
			int N = testdata.length;

			// make prediction
			String predictionOutput = outputDir + "/class.predictions.out";

			for (int i = 0; i < N; i++) {

				// Classify a test vector using the Java object
				// (in a real application, this should not be one of the
				// training vectors)
				double d = model.classify(testdata[i]);

				Consts.fileWriter((int) testdata[i].getLabel() + "\t" + (int) d
						+ "\n", predictionOutput, true);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
