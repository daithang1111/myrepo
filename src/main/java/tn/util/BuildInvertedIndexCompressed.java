package tn.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import edu.umd.cloud9.io.pair.PairOfStringInt;
import edu.umd.cloud9.util.fd.Object2IntFrequencyDistribution;
import edu.umd.cloud9.util.fd.Object2IntFrequencyDistributionEntry;
import edu.umd.cloud9.util.pair.PairOfObjectInt;

public class BuildInvertedIndexCompressed extends Configured implements Tool {
	private static final Logger LOG = Logger
			.getLogger(BuildInvertedIndexCompressed.class);

	/**
	 * 
	 * @author nguyentd4
	 * 
	 */
	private static class MyMapper extends
			Mapper<LongWritable, Text, PairOfStringInt, IntWritable> {
		private static final PairOfStringInt KEY = new PairOfStringInt();
		private static final IntWritable VALUE = new IntWritable();
		private static final Object2IntFrequencyDistribution<String> COUNTS = new Object2IntFrequencyDistributionEntry<String>();

		/**
		 * map
		 */
		@Override
		public void map(LongWritable docno, Text doc, Context context)
				throws IOException, InterruptedException {
			String text = doc.toString();
			COUNTS.clear();
			String[] terms = text.split("\\s+");
			// First build a histogram of the terms.
			for (String term : terms) {
				if (term == null || term.length() == 0) {
					continue;
				}
				COUNTS.increment(term);
			}
			// Emit postings.
			for (PairOfObjectInt<String> e : COUNTS) {
				KEY.set(e.getLeftElement(), (int) docno.get());
				VALUE.set(e.getRightElement());
				context.write(KEY, VALUE);
				// emit additional value to count df
				KEY.set(e.getLeftElement(), -1);// this can be 0, but i'm not
												// sure so
				// MIN_VALUE is best
				VALUE.set(1);
				context.write(KEY, VALUE);
			}
		}
	}

	/**
	 * 
	 * @author nguyentd4
	 * 
	 */
	private static class MyReducer extends
			Reducer<PairOfStringInt, IntWritable, Text, BytesWritable> {
		// private final static IntWritable DF = new IntWritable();
		private final static IntWritable prevDocNo = new IntWritable();
		private final static Text PREV = new Text();
		private final static ByteArrayOutputStream baos = new ByteArrayOutputStream();
		private final static DataOutputStream dos = new DataOutputStream(baos);

		// private final static ArrayListWritable<PairOfInts> POSTINGs = new
		// ArrayListWritable<PairOfInts>();
		/**
		 * reduce
		 */
		@Override
		public void reduce(PairOfStringInt key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int docNumber = key.getRightElement();
			String word = key.getLeftElement();
			if ((PREV.getLength() > 0) && (!word.equals(PREV.toString()))) {
				context.write(PREV, new BytesWritable(baos.toByteArray()));
				dos.flush();
				baos.reset();
				prevDocNo.set(0);//
			}
			Iterator<IntWritable> iter = values.iterator();
			if (docNumber == -1) {
				int df = 0;
				while (iter.hasNext()) {
					df += iter.next().get();
				}
				WritableUtils.writeVInt(dos, df);
			} else {
				// do normal thing
				while (iter.hasNext()) {
					WritableUtils.writeVInt(dos, key.getRightElement()
							- prevDocNo.get());
					WritableUtils.writeVInt(dos, iter.next().get());
					prevDocNo.set(key.getRightElement());
				}
			}
			PREV.set(word);
		}

		/**
		 * cleanup
		 */
		@Override
		public void cleanup(Context context) throws IOException,
				InterruptedException {
			context.write(PREV, new BytesWritable(baos.toByteArray()));
			baos.reset();
			dos.close();
		}
	}

	/**
	 * 
	 * @author nguyentd4
	 * 
	 */
	protected static class MyPartitioner extends
			Partitioner<PairOfStringInt, IntWritable> {
		/**
		 * getPartition, by the term
		 */
		@Override
		public int getPartition(PairOfStringInt key, IntWritable value,
				int numReduceTasks) {
			return (key.getLeftElement().hashCode() & Integer.MAX_VALUE)
					% numReduceTasks;
		}
	}

	private BuildInvertedIndexCompressed() {
	}

	private static final String INPUT = "input";
	private static final String OUTPUT = "output";
	private static final String NUM_REDUCERS = "numReducers";

	/**
	 * Runs this tool.
	 */
	@SuppressWarnings({ "static-access" })
	public int run(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("path").hasArg()
				.withDescription("input path").create(INPUT));
		options.addOption(OptionBuilder.withArgName("path").hasArg()
				.withDescription("output path").create(OUTPUT));
		options.addOption(OptionBuilder.withArgName("num").hasArg()
				.withDescription("number of reducers").create(NUM_REDUCERS));
		CommandLine cmdline;
		CommandLineParser parser = new GnuParser();
		try {
			cmdline = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Error parsing command line: "
					+ exp.getMessage());
			return -1;
		}
		if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(OUTPUT)) {
			System.out.println("args: " + Arrays.toString(args));
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(120);
			formatter.printHelp(this.getClass().getName(), options);
			ToolRunner.printGenericCommandUsage(System.out);
			return -1;
		}
		String inputPath = cmdline.getOptionValue(INPUT);
		String outputPath = cmdline.getOptionValue(OUTPUT);
		int reduceTasks = cmdline.hasOption(NUM_REDUCERS) ? Integer
				.parseInt(cmdline.getOptionValue(NUM_REDUCERS)) : 1;
		LOG.info("Tool name: "
				+ BuildInvertedIndexCompressed.class.getSimpleName());
		LOG.info(" - input path: " + inputPath);
		LOG.info(" - output path: " + outputPath);
		LOG.info(" - num reducers: " + reduceTasks);
		
		Job job = Job.getInstance(getConf());
		
		job.setJobName(BuildInvertedIndexCompressed.class.getSimpleName());
		job.setJarByClass(BuildInvertedIndexCompressed.class);
		
		job.setNumReduceTasks(reduceTasks);
		
		FileInputFormat.setInputPaths(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		
		job.setMapOutputKeyClass(PairOfStringInt.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(BytesWritable.class);
		
		job.setOutputFormatClass(MapFileOutputFormat.class);
		job.setMapperClass(MyMapper.class);
		job.setReducerClass(MyReducer.class);
		job.setPartitionerClass(MyPartitioner.class);
		// Delete the output directory if it exists already.
		Path outputDir = new Path(outputPath);
		FileSystem.get(getConf()).delete(outputDir, true);
		long startTime = System.currentTimeMillis();
		job.waitForCompletion(true);
		System.out.println("Job Finished in "
				+ (System.currentTimeMillis() - startTime) / 1000.0
				+ " seconds");
		return 0;
	}

	/**
	 * Dispatches command-line arguments to the tool via the {@code ToolRunner}.
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new BuildInvertedIndexCompressed(), args);
	}
}