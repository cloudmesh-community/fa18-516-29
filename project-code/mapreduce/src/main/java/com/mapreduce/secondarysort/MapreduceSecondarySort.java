package com.mapreduce.secondarysort;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;



/** Wrapper class for all the static classes */

public class MapreduceSecondarySort {

	/**
	 * Static class which defines the composite key for map reduce and has custom comparator to do the
	 * sorting based on the composite key and not only the natural key in reduce phase.
	 */

	public static class Compositekey implements WritableComparable<Compositekey> {

		private int first = 0;
		private int second = 0;

		/**
		 * Set the left and right values.
		 */
		public void set(int left, int right) {
			first = left;
			second = right;
		}

		public int getFirst() {
			return first;
		}

		public int getSecond() {
			return second;
		}

		public void readFields(DataInput in) throws IOException {
			first = in.readInt();
			second = in.readInt();
		}

		public void write(DataOutput out) throws IOException {
			out.writeInt(first);
			out.writeInt(second);

		}

		@Override
		public int hashCode() {
			return first * 157 + second;
		}

		@Override
		public boolean equals(Object right) {
			if (right instanceof Compositekey) {
				Compositekey r = (Compositekey) right;
				return r.first == first && r.second == second;
			} else {
				return false;
			}
		}

		/** Comparator to compare serialized Compositekey */

		public static class Comparator extends WritableComparator {

			public Comparator() {
				super(Compositekey.class);
			}

			public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
				return compareBytes(b1, s1, l1, b2, s2, l2);
			}
		}

		static { // register this comparator
			WritableComparator.define(Compositekey.class, new Comparator());
		}

		public int compareTo(Compositekey o) {
			if (first != o.first) {
				return first < o.first ? -1 : 1;
			} else if (second != o.second) {
				return second < o.second ? -1 : 1;
			} else {
				return 0;
			}
		}

	}

	/**
	 * Custom partitioner for sending all the data belonging to the same natural key to
	 * the same reducer
	 */

	public static class Custompartitioner extends Partitioner<Compositekey, IntWritable>

	{

		@Override
		public int getPartition(Compositekey key, IntWritable value, int numpartitions) {
			return Math.abs(key.getFirst() * 127) % numpartitions;
		}

	}
   /** This class is for the framework to compare the natural key values in the reducer*/
	
	public static class NaturalkeyGroupingComparator implements RawComparator<Compositekey> {

		@Override
		public int compare(Compositekey o1, Compositekey o2) {
			int l = o1.getFirst();
			int r = o2.getFirst();
			return l == r ? 0 : (l < r ? -1 : 1);
		}

		@Override
		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
			return WritableComparator.compareBytes(b1, s1, Integer.SIZE / 8, b2, s2, Integer.SIZE / 8);
		}
	}

	/**
	 * Mapper reads two integers from each line and generate a key, value pair as
	 * ((left,right), right).
	 */

	public static class MapClass extends Mapper<LongWritable, Text, Compositekey, IntWritable> {

		private final Compositekey key = new Compositekey();
		private final IntWritable value = new IntWritable();

		public void map(LongWritable inkey, Text invalue, Context context) throws IOException, InterruptedException {

			String[] tokens = invalue.toString().split(",");
			int left = 0;
			int right = 0;
			left = Integer.parseInt(tokens[0]);
			right = Integer.parseInt(tokens[1]);
				

				key.set(left, right);
				value.set(right);
				context.write(key, value);
			}
		}
	

	/** Reducer to emit the list of values */

	public static class Reduce extends Reducer<Compositekey, IntWritable, Text, IntWritable> {

		private final Text first = new Text();

		public void reduce(Compositekey key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {

			first.set(Integer.toString(key.getFirst()));
			for (IntWritable value : values) {
				context.write(first, value);
			}

		}
	}

	/**
	 * Driver method
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */

	public static void main(String args[]) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: secondarysort <in> <out>");
			System.exit(2);
		}

		Job job = Job.getInstance(conf, "secondary sort");
		job.setJarByClass(MapreduceSecondarySort.class);
		job.setMapperClass(MapClass.class);
		job.setReducerClass(Reduce.class);

		// group and partition by the first int in the pair as this is the natural key of the data
		job.setPartitionerClass(Custompartitioner.class);
		job.setGroupingComparatorClass(NaturalkeyGroupingComparator.class);

		// the map output is Compositekey, IntWritable
		job.setMapOutputKeyClass(Compositekey.class);
		job.setMapOutputValueClass(IntWritable.class);

		// the reduce output is Text, IntWritable
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}
