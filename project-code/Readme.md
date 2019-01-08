
## Steps to run the Secondary Sort program


#### Introduction:
 The below section explains how to compile and run the mapreduce and spark jars for the secondary sort program.
 
#### Steps:
 
  1. Take the file secondarysorttestfile from the project-report/datafiles folder aqnd copy it to the Namonode machine through winscp,     where the spark installation is done.
  
  2. Make a directory in hdfs and copy the file to the hdfs file system by the below commands:
  
  hadoop fs -mkdir /user/externaltables/secondarysortdata
  
  hadoop fs -copyFromLocal /home/ubuntu/datafiles/secondarysorttestfile /user/externaltables/secondarysortdata
  
  3. create the 2 jars from the project-code folder Mapreduce and Spark by using maven.(Maven should be installed in the local system)
  
      Go to the folder \fa18-516-29\project-code\mapreduce and run the below command:
    
      mvn package
    
      Go to the folder \fa18-516-29\project-code\spark and run the below command:
    
      mvn package
    
     This will create 2 folder named target in both the directories where the jar file is built with names hadoop-spark-examples-0.0.1-SNAPSHOT.jar and spark-examples-0.0.1-SNAPSHOT.jar
     
   4. copy these jar files to a directory of the namenode machine through winscp.
   
   5. Go to the above diretoty location where the jar files have been copied and run the below command for running secondary sort
   in mapreduce:
   
   yarn jar  /home/ubuntu/projectjars/hadoop-spark-examples-0.0.1-SNAPSHOT.jar com.mapreduce.secondarysort.MapreduceSecondarySort /user/externaltables/secondarysortdata/secondarysorttestfile /user/logs1
   
   6. Check the output of the secondary sort in /user/logs1/part-00000 file which will be a soorted file on both the first and second columns.
   
   7. Run the jar for the spark secondary sort by the below command:
   
   spark-submit --deploy-mode client --class com.spark.secondarysort.SparkSecondarySort  /home/ubuntu/projectjars/spark-examples-0.0.1-SNAPSHOT.jar /user/externaltables/secondarysortdata/secondarysorttestfile /user/logs2
   
   8. Check the output of the spark secondary sort in /user/logs2/part-00000 file which will be a soorted file on the second column
   which is the value column.
   
   
      
