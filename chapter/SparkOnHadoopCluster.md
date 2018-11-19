# Installation and Configuration of Spark in a Multi node Hadoop Cluster


## Introduction:
 The following section describes the installation and configuration of Spark on Hadoop cluster.
 | github: [:cloud:](https://github.com/cloudmesh-community/fa18-516-29/blob/master/project-paper/report.md)

## Motivation

Spark by design is a Distributed Computing Engine and creates an immutable resilient dataset RDD in memory on top of the data in 
the underlying file system.These RDDs are partitioned and loaded into the memory of all the nodes in the cluster.The computation
from one stage to another happens in memory itself by streaming data to subsequent RDDs without the need of writing the intermediate data to the file system as in the case of traditional map reduce where the output of each map phase is written to HDFS resulting in a
lot of Physical I/O.Spark minimizes this Physical I/O and does the entire computation in memory making the processing much faster.
Almost all the contemporary Big Data platforms today are using Hadoop as a storage and Spark as a Compute Engine.Spark integrates very well with Hive through HiveContext and Spark SQL is used to write Procedural SQL code on data described in relational format in Hive by importing them in Spark Dataframes which is an abstraction over RDD.

## Software
 Spark-2.3.2
 
## Keywords
 Amazon EC2,Hadoop,Spark,Hive
 
## Installation

i. Download and install spark 2.3.2.
    wget https://www-eu.apache.org/dist/spark/spark-2.3.2/spark-2.3.2-bin-hadoop2.7.tgz -P ~/spark_installation
    
 ii. Untar the zip file in SPARK_HOME
     tar zxvf spark-2.3.2-bin-hadoop2.7.tgz -C ~/spark_home
     
 iii. set the env variables in .profile and .bashrc
 
      export SPARK_HOME=/home/ubuntu/spark_home/spark-2.3.2-bin-hadoop2.7
      export PATH=$PATH:$SPARK_HOME/bin
      export SPARK_CONF_DIR=/home/ubuntu/spark_home/spark-2.3.2-bin-hadoop2.7/conf

## Spark configuration in spark-defaults.conf for a 4GB(memory) node:
   
        spark.master                     yarn
        spark.executor.memory            2g
        spark.eventLog.enabled           false
        spark.serializer                 org.apache.spark.serializer.KryoSerializer
        spark.yarn.executor.memoryOverhead 384m
        spark.yarn.submit.file.replication 1
        spark.yarn.stagingDir  /home/ubuntu/yarnstage
        spark.yarn.historyServer.address ${hadoopconf-yarn.resourcemanager.hostname}:18080
        spark.dynamicAllocation.enabled false
        
        
 ## Running Spark
    Test that spark is getting launched through yarn by running the spark wordcount job in client mode.
   
   spark-submit --deploy-mode client --class org.apache.spark.examples.JavaWordCount $SPARK_HOME/examples/jars/spark-examples_2.11-  2.3.2.jar /user/externaltables/testdata/testfile
   
   ## Spark configuration in spark-defaults.conf for a 8GB(memory) node:
   
   For a T2.large instance which is 8GB memory node and the yarn.scheduler.maximum-allocation-mb is configured as 6GB,we can do 
   the following configuration for spark.
    
       spark.executor.memory            4g
       spark.yarn.executor.memoryOverhead 384m
       
    We have to keep the executor memory + overhead less than the one allocated to the yarn.scheduler.maximum-allocation-mb
   
   
   
