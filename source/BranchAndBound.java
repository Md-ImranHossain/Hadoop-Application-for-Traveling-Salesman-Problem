package main;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class BranchAndBound {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: Branch and Bound <in> <out>");
			System.exit(0);
		}
                Configuration conf = new Configuration();
                Job job = new Job(conf, "Branch and Bound");
		job.setJarByClass(BranchAndBound.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
                System.exit(job.waitForCompletion(true)?0:1);
	}
}