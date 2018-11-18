# Hadoop 4-node cluster set up on AWS EC2 instances-Part 2(Hadoop Installation and Configuration)

## Introduction
The following section describes the installation of Hadoop on the 4 instances and the configurations required for the different config files for Hdfs,MapReduce and Yarn.

| github: [:cloud:](https://github.com/cloudmesh-community/fa18-516-29/blob/master/project-paper/report.md)

## Cluster Planning:
We will have a 4 node Hadoop cluster with 1 as master and 3 as slaves.Each instance is T2.meduim type and has 4 GB of memory
and initially 8GB of physical storage.We will add more volume and upgrade the physical storage to 16GB.This configuration is fine for doing the initial cluster set up and running some map reduce jobs for test purpose,but definitely will not work for large datasets.For a 
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
   
   i. Before starting the installation,update all the servers as a good practice by the below command:
        sudo apt-get update
   
   ii. Install java version8 in all the servers:
        sudo apt install openjdk-8-jdk
    
   iii. Download and install hadoop 2.9 on all the servers.
        wget http://apache.mirrors.tds.net/hadoop/common/hadoop-2.9.1/hadoop-2.9.1.tar.gz -P ~/hadoop_installation
    
   iv. Uncompress the tar file in any directory called hadoop_home
        tar zxvf ~/hadoop_installation/hadoop-* -C ~/hadoop_home
 
   v. set up the env variables in the .profile and .bashrc of all the servers
 
        export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
        export PATH=$PATH:$JAVA_HOME/bin
        export HADOOP_HOME=/home/ubuntu/hadoop_home/hadoop-2.9.1
        export PATH=$PATH:$HADOOP_HOME/bin
        export HADOOP_CONF_DIR=/home/ubuntu/hadoop_home/hadoop-2.9.1/etc/hadoop
   
   vi. Load profile in all the servers
        ~/.profile
     
   vii. Change the hadoop-env.sh in $HADOOP_HOME/etc/hadoop in all the instances to add the below line for JAVA_HOME.
          export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
          
   ## Hadoop configurations in $HADOOP_CONF_DIR:(for a cluster where nodes have only 4GB of memory)
        (Add the below values in the config files in all the instances):
       
     i. core-site.xml
    
        fs.defaultFS     hdfs://ec2-52-24-204-101.us-west-2.compute.amazonaws.com:9001(This is the Master instance hostname,
                       9001 is the RPC port)
                       
        hadoop.tmp.dir  file:///home/ubuntu/hadoop_home/hadoop-2.9.1/hadoop_tmp(The filesystem should be the one where we can add more
        volume if required)
                       
.   ii. hdfs-site.xml

          dfs.namenode.name.dir  file:///home/ubuntu/hadoop_home/hadoop-2.9.1/hadoop_data/hdfs/namenode
                             (This is the namenode directory in the Master instance which will have the fs image and edit logs)
                             
          dfs.datanode.data.dir  file:///home/ubuntu/hadoop_home/hadoop-2.9.1/hadoop_data/hdfs/datanode
                             (This is the data directory in the slave nodes with the actual data blocks of Distributed file system)
                             
          dfs.replication       2 (Initially only 2 considering the small size of physical storage)
        
     iii. mapred-site.xml
     
            mapreduce.framework.name   yarn  (Hadoop 2.9 has Yarn framework)
            yarn.app.mapreduce.am.resource.mb 3072   
            mapreduce.map.memory.mb  512
            mapreduce.reduce.memory.mb 512
          
     iv. yarn-site.xml
       
           yarn.acl.enable  false
           yarn.resourcemanager.hostname ec2-52-24-204-101.us-west-2.compute.amazonaws.com
           yarn.nodemanager.aux-services mapreduce_shuffle
           yarn.nodemanager.vmem-check-enabled false
           yarn.nodemanager.resource.memory-mb 3072
           yarn.scheduler.maximum-allocation-mb 3072
           yarn.scheduler.minimum-allocation-mb 1536
           yarn.nodemanager.local-dirs ${hadoop.tmp.dir}/nm-local-dir
