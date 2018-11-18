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

masters file      namenode                          
slaves file       datanode1 datanode2 datanode3
                                            
## Software
 Java 8,Hadoop 2.9
 
## Keywords
 Hadoop 2.9 Multi Node Cluster
 
 ## Installation steps:
   (Everything is done when logged in as user ubuntu)
   
   i. Before starting the installation,update all the servers as a good practice by the command:
        sudo apt-get update
   
   ii. Install Java version8 in all the servers:
        sudo apt install openjdk-8-jdk
    
   iii. Download and install Hadoop 2.9 on all the servers:
        wget http://apache.mirrors.tds.net/hadoop/common/hadoop-2.9.1/hadoop-2.9.1.tar.gz -P ~/hadoop_installation
    
   iv. Uncompress the tar file in any directory called hadoop_home:
        tar zxvf ~/hadoop_installation/hadoop-* -C ~/hadoop_home
 
   v. set up the env variables in the .profile and .bashrc of all the servers:
 
        export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
        export PATH=$PATH:$JAVA_HOME/bin
        export HADOOP_HOME=/home/ubuntu/hadoop_home/hadoop-2.9.1
        export PATH=$PATH:$HADOOP_HOME/bin
        export HADOOP_CONF_DIR=/home/ubuntu/hadoop_home/hadoop-2.9.1/etc/hadoop
   
   vi. Load profile in all the instances:
        ~/.profile
     
   vii. Change the hadoop-env.sh in $HADOOP_HOME/etc/hadoop in all the instances to add the below line for JAVA_HOME:
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
           
   ##  Hadoop Namenode Format
          After configuring the config files,format the namenode:
          
          hdfs namenode -format  (Format resets the namenode and should only be done once in the lifetime of a cluster)
      
   ## Hadoop deamons:
      
        Start the following deamons:
        
        On Namenode:
         $HADOOP_HOME/sbin/hadoop-daemon.sh --config $HADOOP_CONF_DIR --script hdfs start namenode
         $HADOOP_HOME/sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR start resourcemanager
         $HADOOP_HOME/sbin/mr-jobhistory-daemon.sh --config $HADOOP_CONF_DIR start historyserver
         The above 3 deamons will run on the Master node.
         
        On DataNode1:
         $HADOOP_HOME/sbin/hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs start datanode
         The datanode process just needs to be started in 1 slave machine DataNode1.This will start the process in another 
         nodes on its own through ssh connection.
         
        On DataNode1,DataNode2,DataNode3:
         $HADOOP_HOME/sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR start nodemanager
         The nodemanager process needs to be started in all the slave machines.
         
   ## Creating directories in file system:
        
         hadoop fs -mkdir /user/externaltables/testdata
         hadoop fs -copyFromLocal /home/ubuntu/datafiles/testfile /user/externaltables/testdata/testfile
         
   ## Run the map reduce job to test that the containers are getting launched properly in all the nodes:
         
         yarn jar hadoop-mapreduce-examples-2.9.1.jar wordcount /user/externaltables/testdata/testfile /user/logs
    
   ## Issues and resolution: 
       
       One very common issue which can come when the containers are launced on the nodes is that of lack of space in the temp 
       storage for the intermediate data which is generated by the mapreduce framework when the job runs.The error looks like:
     
       [INFO] Diagnostics: No space available in any of the local directories.
     
       To resolve this issue,find the filesystem to which hadoop.temp.dir is mapped to:
       
        df /home/ubuntu/hadoop_home/hadoop-2.9.1/hadoop_tmp will show the below ext4 filesystem:
       
       /dev/xvda1
       
        df -h will show the space available in the above file system.If it is nearly full,then take more volume for the EC2
        instances by following the below steps:
       
       i. On the left side in Elastic Block Store,go to Volumes.Add the number of volumne required(another 8GB) to all the instances.
          All this will be mapped to the drive /dev/xvda.
          
       ii. Login to putty of the machines and run lsblk.This will show something like below:
           xvda    202:0    0   16G  0 disk
            └─xvda1 202:1    0   8G  0 part /
            
       iii. Increase the partition size of xvda1
            growpart /dev/xvda 1
           
       iv. Resize the filesystem:
           resize2fs /dev/xvda1
           
       v. Do lsblk and confirm the partition size.It should be something like below:
           xvda    202:0    0   16G  0 disk
            └─xvda1 202:1    0   16G  0 part /
           
       vi. Do df -h and confirm that the space allocated to the filesystem /dev/xvda1 is increased by 8GB.
       
   ## Hadoop configuration for a 8GB(Memory) node cluster:
      This is feasible if we are taking T2.large instances.
        
         filename          config key                          value in mb
         
         mapred-site.xml   yarn.app.mapreduce.am.resource.mb    2048
         mapred-site.xml   mapreduce.map.memory.mb              1024
         mapred-site.xml   mapreduce.reduce.memory.mb           1024
         yarn-site.xml     yarn.nodemanager.resource.memory-mb  6144
         yarn-site.xml     yarn.scheduler.maximum-allocation-mb 6144
         yarn-site.xml     yarn.scheduler.minimum-allocation-mb 1024
          
