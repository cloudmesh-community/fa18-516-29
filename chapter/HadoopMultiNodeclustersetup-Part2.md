# Hadoop 4-node cluster set up on AWS EC2 instances-Part 2(Hadoop installation and configuration)

## Introduction
The following section describes the installation of Hadoop on the 4 instances and the configurations required for the different config files for Hdfs,MapReduce and Yarn.

| github: [:cloud:](https://github.com/cloudmesh-community/fa18-516-29/blob/master/project-paper/report.md)

## Cluster Planning:
We will have a 4 node Hadoop cluster with 1 as master and 3 as slaves.Each instance is T2.meduim type and has 4 GB of memory
and initially 8GB of physical storage.We will add more volume and upgrade the physical storage to 16GB.This configuration is fine for doing 
the initial cluster set up and running some map reduce jobs for test purpose,but definitely will not work for large datasets.For a 
substantial amount of data transfer,one should go for T2.Xlarge instances which have 16GB of memory and initially 32 GB of physical storage.The only downside is that it will be expensive to own 4 such large instances in EC2 for a developer.

## Master and Slaves:
There will be 1 Master instance.This will have the Namemode,Resourcemanager and Jobhistoryserver deamons running.Ideally,the Resource manager and Job history server runs on different machines in actual production environment,but here we will set up only 1 Master node
for HDFS as well as Yarn.There is no Secondary NameNode in our set up.There will be 3 Slave nodes where Datanode and Nodemanager deamons will run.To configure Masters and Slaves create 2 files with names masters and slaves inside $HADOOP_CONF_DIR in each instance.Add the following to them.

masters file                               slaves file
namenode                                    datanode1
                                            datanode2
                                            datanode3
                                            
## Software
 Java 8,Hadoop 2.9
 
## Keywords
 Hadoop 2.9 Multi Node Cluster
 
 ## Installation steps:
   (Everything is done when logged in as user ubuntu)
