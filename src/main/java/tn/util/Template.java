package tn.util;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Template extends Configured implements Tool {

	/**
	 * Dispatches command-line arguments to the tool via the {@code ToolRunner}.
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Template(), args);
	}

	@Override
	public int run(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
}
