package com.spark.secondarysort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;


import scala.Tuple2;

public class SparkSecondarySort {

	public static void main(String args[]) {

		if (args.length < 2) {
			System.err.println("Usage: SparkSecondarySort <infile> <outfile>");
			System.exit(1);
		}

		String inputFile = args[0];
		String outputDirectory = args[1];

		// create Spark context with Spark configuration
		JavaSparkContext sc = new JavaSparkContext(new SparkConf().setAppName("Secondary Sort"));

		JavaRDD<String> lines = sc.textFile(inputFile, 1);

		JavaPairRDD<Integer, Integer> pairs = lines.mapToPair(new PairFunction<String, Integer, Integer>() {
			public Tuple2<Integer, Integer> call(String t) throws Exception {

				String[] tokens = t.split(","); //2001,40 

				Tuple2<Integer, Integer> values = new Tuple2<Integer, Integer>(Integer.parseInt(tokens[0]),
						Integer.parseInt(tokens[1]));

				return values;
			}
		});

		JavaPairRDD<Integer, Iterable<Integer>> groups = pairs.groupByKey();

		JavaPairRDD<Integer, Iterable<Integer>> sorted = groups
				.mapValues(new Function<Iterable<Integer>, Iterable<Integer>>() {

					public Iterable<Integer> call(Iterable<Integer> vals) throws Exception {
						List<Integer> newList = new ArrayList<Integer>(iterableToList(vals));
						Collections.sort(newList);
						return newList;
					}

				});

		sorted.saveAsTextFile(outputDirectory);

	}

	static List<Integer> iterableToList(Iterable<Integer> iterable) {
		List<Integer> list = new ArrayList<Integer>();
		for (Integer item : iterable) {
			list.add(item);
		}
		return list;
	}

}
