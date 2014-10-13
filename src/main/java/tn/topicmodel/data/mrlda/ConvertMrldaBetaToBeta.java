package tn.topicmodel.data.mrlda;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import tn.topicmodel.data.press.ProcessPressRelease;
import tn.util.Consts;

import com.google.common.base.Joiner;

public class ConvertMrldaBetaToBeta {
	private static final Logger LOG = Logger
			.getLogger(ConvertMrldaBetaToBeta.class);
	private static final String INPUT_OPTION = "mrldaBeta";
	private static final String OUTPUT_OPTION = "outputDir";
	private static final String VOCABSIZE_OPTION = "vocabSize";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("mrldaBeta").hasArg()
				.withDescription("the beta output from mrlda")
				.create(INPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("outputDir").hasArg()
				.withDescription("output dir for text files")
				.create(OUTPUT_OPTION));

		options.addOption(OptionBuilder.withArgName("vocabSize").hasArg()
				.withDescription("size of vocabulary").create(VOCABSIZE_OPTION));

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
				|| !cmdline.hasOption(INPUT_OPTION)
				|| !cmdline.hasOption(VOCABSIZE_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(ProcessPressRelease.class.getCanonicalName(),
					options);
			System.exit(-1);
		}

		String inputFile = cmdline.getOptionValue(INPUT_OPTION);
		String outputDir = cmdline.getOptionValue(OUTPUT_OPTION);
		int vocabSize = -1;

		try {
			vocabSize = Integer.parseInt(cmdline
					.getOptionValue(VOCABSIZE_OPTION));
		} catch (Exception pe) {
			pe.printStackTrace();
			LOG.info("invalid vocab size");
			return;
		}

		// convert data
		String outputFile = outputDir + "/beta.txt";
		List<String> mrldaBeta = Consts.readFileAsList(inputFile);
		for (int i = 5; i < mrldaBeta.size(); i++) {
			if (i % 4 == 3) {
				Consts.fileWriter(getLdacBeta(mrldaBeta.get(i), vocabSize)
						+ "\n", outputFile, true);
			}
		}
	}

	private static String getLdacBeta(String mrldaBeta, int vocabSize) {
		String tmpBeta = mrldaBeta.replace("Value: {", "").replace("}", "");
		String[] values = tmpBeta.split(", ");
		double sum = 0.0;
		HashMap<Integer, Double> hash = new HashMap<Integer, Double>();
		for (int i = 0; i < values.length; i++) {
			String[] index_value = values[i].split("=");

			if (index_value.length != 2) {
				LOG.info("invalid beta file");
				return null;
			} else {
				int index = Integer.parseInt(index_value[0]);
				double value = Double.parseDouble(index_value[1]);
				value = Math.exp(value);
				value += Math.exp(-100);
				hash.put(index, value);
				sum += value;
			}
		}
		LOG.info("SUM is:" + sum);
		List<String> output = new ArrayList<String>();
		for (int i = 0; i < vocabSize; i++) {
			int realIndex = i + 1;
			if (hash.containsKey(realIndex)) {
				try {
					output.add(Math.log(hash.get(realIndex) / sum) + "");
				} catch (Exception e) {
					LOG.info("ERROR");
					e.printStackTrace();
				}
			} else {
				output.add("-100");
			}
		}

		return Joiner.on(" ").join(output);
	}
}
