# Integration of Hive with Spark

## Introduction:
 The following section describes how to integrate Hive with Spark.
 
 | github: [:cloud:](https://github.com/cloudmesh-community/fa18-516-29/blob/master/project-paper/report.md)

## Motivation

 Hive can run on multiple execution engines and can be integrated with Spark so that the underlying computation engine is Spark and not
 Mapreduce.This increases the performance of hive queries greatly as Spark does not write the intermediate data to the local
 filesystem and does the computation in memory by creating immutable partitioned datasets RDDS and streaming data from one
 RDD to another without the need of writing the intermediate results to HDFS.This saves lot of Physical I/O and makes the query   processing much faster.

## Software
 Hive 2.3, Spark 2.3.2
 
## Keywords
 Amazon EC2,Hadoop,Spark,Hive
 
## Integration:
  
    1. Link the following Spark jars to Hive:
  
	ln -s /home/ubuntu/spark_home/spark-2.3.2-bin-hadoop2.7/jars/spark-network-common_2.11-2.3.2.jar /home/ubuntu/hive_home/apache-hive-2.3.3-bin/lib/spark-network-common_2.11-2.3.2.jar

	ln -s /home/ubuntu/spark_home/spark-2.3.2-bin-hadoop2.7/jars/spark-core_2.11-2.3.2.jar /home/ubuntu/hive_home/apache-hive-2.3.3-bin/lib/spark-core_2.11-2.3.2.jar

	ln -s /home/ubuntu/spark_home/spark-2.3.2-bin-hadoop2.7/jars/scala-library-2.11.8.jar /home/ubuntu/hive_home/apache-hive-2.3.3-bin/lib/scala-library-2.11.8.jar
  
  
   2. Do the following configurations in hive-site.xml(For a 8GB memory node)
  
                            	
       hive.execution.engine=spark
       spark.master=yarn-cluster
       spark.eventLog.enabled=TRUE
       spark.eventLog.dir=file:///home/ubuntu/hive_home/apache-hive-2.3.3-bin/spark_logs
       spark.executor.memory=4g
       spark.yarn.executor.memoryOverhead=750	
       spark.serializer=org.apache.spark.serializer.KryoSerializer	 

  3. Configure yarn-site.xml with the Fair Scheduler:

     yarn.resourcemanager.scheduler.class=org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler


## Running Hive on Spark:	  
	  
      1. Create directories in HDFS for storing Hive data as an external table
     
        hadoop fs -mkdir /user/externaltables/insurancedata
        hadoop fs -copyFromLocal insurance_datafile /user/externaltables/insurancedata/
        
      2. Start the hive terminal by typing command Hive
         hive
         
      3. Create an External table in Hive pointing to the file in HDFS:
         
         create external table if not exists insurance_data_1(
         policyID int,
         statecode char(2),
         county string,
         eq_site_limit decimal,
         hu_site_limit decimal,
         fl_site_limit decimal,
         fr_site_limit decimal,
         tiv_2011 decimal,
         tiv_2012 decimal,
         eq_site_deductible decimal,
         hu_site_deductible decimal,
         fl_site_deductible decimal,
         fr_site_deductible decimal,
         point_latitude decimal,
         point_longitude decimal,
         line string,
         construction string,
         point_granularity int)
         comment 'Test data about insurance data'
         row format delimited
         fields terminated by ','
         stored as textfile
         location '/user/externaltables/insurancedata/';
         
       4. Run a query in debug mode to count the number of policies:
       
           hive --hiveconf hive.root.logger=DEBUG,console -e "select count(policyID) from insurance_data_1"
	  
	5. We can see the spark jobs being launched on the web ui:
	   
	   http://ec2-52-24-204-101.us-west-2.compute.amazonaws.com:4040
