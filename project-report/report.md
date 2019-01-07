# Hadoop, Hive and Spark multi node Cluster set up on Amazon EC2 instances

| Shilpa Singh
| shilsing@iu.edu
| Indiana University Bloomington
| hid: fa18-516-29
| github: [:cloud:](https://github.com/cloudmesh-community/fa18-516-29/tree/master/project-report/report.md)
| code: [:cloud:](https://github.com/cloudmesh-community/fa18-516-29/tree/master/project-code)

---

Keywords: hadoop, hive, Spark, AWS

---

## Abstract

The goal of this project is to demonstrate the steps needed to set up a multi node Hadoop Cluster with Spark and Hive on Amazon EC2 instances from the scratch and do comparison between the traditional mapreduce and Distributed Computing through Spark. It details the Hadoop and Spark configurations required for a 8GB node Hadoop cluster for developmental and testing purpose.The objective is to do all the installations and configurations from the scratch.This process will be the same as doing the installation and set up on any 4 unix machines which have a static IP. 
We will also show how to integrate spark as a compute engine in hive and bypass mapreduce in hive.
Lastly, we demostrate a sorting technique called "sort by value" where we sort data in large files based on a specified column by making use of map reduce framework without bringing all the data in memory of one node in a distributed environment.



## Introduction

Today,most of the Big data projects are leveraging Hadoop,Hive and Spark to foster an ETL environment in production and this project
is an approach to set up a multi node distributed environment in AWS EC2 instances and run some map-reduce jobs in this environment.
The project describes what are the minimum configurations required for a multi node Hadoop cluster set-up with Spark and Hive in AWS EC2 instances and how to establish a passwordless ssh connection between the instances.This is applicable for any unix/linux instances which can have a static IP and the same steps need to be followed for establishing a passwordless ssh connection and configuring the hadoop and Spark config files.The recommended amount of memory for an instance is 8GB and atleast 20GB of physical disk space.

## Software versions

* Hadoop 2.9.1
* Hive 2.3.3
* Spark 2.3.2
* Java 8
	

## Instance configurations and passwordless ssh set up


### Motivation:
Before setting up a Hadoop cluster,it is important to set up connection between our client machine and the EC2 instances as 
well as passwordless ssh communication between the EC2 instances.Also,the IP of EC2 instances changes whenever we restart the servers.Since,we will need to stop and restart the machines only when we are using them,it is important to set up Elastic IPs for these instances,which do not change on restart else our passwordless ssh connection set up will not work.


### Cluster Planning:
We will set up a 4 node Hadoop cluster with 1 node as master and 3 as slaves.Each instance is T2.large type and has 8 GB of memory
and initially 16GB of physical storage.This configuration is fine for doing the initial cluster set up and running some map reduce jobs for testing purpose,but definitely will not work for large datasets.For a substantial amount of data transfer,one should go for T2.Xlarge instances which have 16GB of memory and initially 32 GB of physical storage.The only downside is that it will be expensive to own 4 such large instances in EC2 for a developer.


### Memory and Disk Requirements:

In AWS,create 4 EC2 instances of type T2.large by following the guidelines given in EC2 manual of how to create an instance.The T2 large instance has 8GB of RAM.In order to launch containers in Hadoop and Spark on a yarn cluster,we found that 4GB of memory in the nodes is the mimimum required.If the memory is below this limit,the Application Master on the slave nodes will not start and the jobs will always be in ACCEPTED state.They will never get allocated to the Application master in slave nodes by the Resource Manager running in master node due to lack of resources.In this scenario also we have to specify the memory configurations in yarn-site.xml and mapreduce-site.xml for each process as Hadoop defaults do not work properly if the memory is less than 8GB.So, we chose to start with T2.large instances where the memory is 8GB.The physical storage of EC2 instances is 16GB and everything is mapped to the drive /dev/xvda.There are few challenges here as there are a lot of log files and local intermediate data which gets written in the datanodes when a mapreduce or spark job runs and this space also gets used up quickly.In our experiment,we found that atleast 11GB of physical storage was needed to start the containers which can get used up as more and more map reduce job runs.So,we chose to add more volume to T2.large instance in the physical storage whenever the space was getting full.This is feasible as EC2 instances have EBS storage and volume can be added on the fly.

### Configuring Elastic Ips for the instances:

After allocating 4 instances,1 Namenode and 3 DataNodes of type T2.large,go to the Network and Security section of the EC2 Management console and allocate 4 new Elastic IPs from the IPv4 address pool.After this IP is allocated,map these IPs to the instances.This is important as the IPs in cloud are dynamic and change whenever we stop our machines.So,the set up will not work when we restart the instances again.Inorder to avoid this issue,we first allocate Elastic Ips to our instances.

![4 node cluster on AWS EC2](images/EC2_instances.png){#fig:1 Namenode and 3 Datanodes in AWS EC2}


![AWS EC2 Elastic IP](images/Elastic_IPs.png){#fig:Elastic Ip allocation for AWS EC2 instances}


### Configuring ssh connection between the client machine and EC2 instances:

The first step after creating EC2 ubuntu instances is to arrange for login through putty.For this,get the client-keypair.pem file from AWS while creating instances.Use Putty keygen feature to generate .ppk file from the .pem file for an SSH connection.Add .ppk file in SSH Auth session of the all the nodes and save the session information in putty.Now,we can login in the instances with username ubuntu.

### Configuring passwordless ssh communication between the instances:

 i. Create a config file with name config in `~/.ssh` folder with the below entries in all the instances including namenode and   datanodes. Add the below entries in the file:
      
      Host namenode
         HostName ec2-18-236-100-223.us-west-2.compute.amazonaws.com
         User ubuntu
         IdentityFile ~/.ssh/client-keypair.pem
      Host datanode1
         HostName ec2-52-88-18-198.us-west-2.compute.amazonaws.com
         User ubuntu
         IdentityFile ~/.ssh/client-keypair.pem
      Host datanode2
         HostName ec2-54-149-168-101.us-west-2.compute.amazonaws.com
         User ubuntu
         IdentityFile ~/.ssh/client-keypair.pem
      Host datanode3
         HostName ec2-34-216-95-55.us-west-2.compute.amazonaws.com
         User ubuntu
         IdentityFile ~/.ssh/client-keypair.pem
	 
   ii. Copy the keyfile received from aws in the `~/.ssh` folder of all the instances from the local machine through winscp.
         
	   
        ```bash
	~/.ssh/client-keypair.pem
        ```
	   
   iii. Change the permissions of all the file in `~/.ssh` folder to 600 of all the servers. This is a requirement for 
        ssh to work correctly.
	
       
   iv. Go to the `~/.ssh` folder of namenode and run the following command.
          
       ```bash
       ssh-keygen -f ~/.ssh/id_rsa -t rsa -P ""
       ```
	  
	This will create 2 files sshkey_rsa.pub and sshkey_rsa in '~/.ssh' folder. 
        
   v. Copy the contents of sshkey_rsa.pub to authorized_keys file in '~/.ssh' folder of namenode by the below command.
           
	   ```bash
           cat ~/.ssh/sshkey_rsa.pub >> ~/.ssh/authorized_keys
	   ```
      This will create an entry for the user ubuntu in the authorized_keys file.
	   
           
   vi. Copy the file authorized_keys to all the datanodes instances in '~/.ssh' folder through winscp.
        
   vii. From the datanode1 and namenode,do ssh to all the other datanodes.It will ask to enter the host names to known_hosts file.
   Confirm yes.
        
   viii. In the /etc/hosts file of all the instances add the following:
        
              172.31.21.154 ec2-52-24-204-101.us-west-2.compute.amazonaws.com
              172.31.16.132 ec2-52-38-172-19.us-west-2.compute.amazonaws.com
              172.31.19.37 ec2-52-42-185-237.us-west-2.compute.amazonaws.com
              172.31.30.216 ec2-52-89-22-141.us-west-2.compute.amazonaws.com
         
	 where the first IP is the Private IP and second is the Public DNS (IPv4) in EC2 management console.
         
   ix. Change the hostname in all the instances to the public DNS name after login because by default they are the private IPs.
         
              sudo hostname ec2-52-24-204-101.us-west-2.compute.amazonaws.com.
	      
       This is very important because the instances know each other by their public DNS names.Repeat this on all the instances.
        
   The above steps complete the set up of passwordless ssh connection between all the instances.
   
   ## Hadoop Installation and Configuration
   
   ### Installation steps:
   (Everything is done when logged in as user ubuntu)
   
   i. Before starting the installation,update all the servers as a good practice by the command:
   
        ```bash
	sudo apt-get update
	```
   
   ii. Install Java version8 in all the servers:
        
	```bash
	sudo apt install openjdk-8-jdk
	```
    
   iii. Download and install Hadoop 2.9 on all the servers:
   
       ```bash
       wget http://apache.mirrors.tds.net/hadoop/common/hadoop-2.9.1/hadoop-2.9.1.tar.gz -P ~/hadoop_installation
       ```
    
   iv. Uncompress the tar file in any directory called hadoop_home:
        
	```bash
	tar zxvf ~/hadoop_installation/hadoop-* -C ~/hadoop_home
	```
 
   v. set up the env variables in the .profile and .bashrc of all the servers:
 
        ```bash
	export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
        export PATH=$PATH:$JAVA_HOME/bin
        export HADOOP_HOME=/home/ubuntu/hadoop_home/hadoop-2.9.1
        export PATH=$PATH:$HADOOP_HOME/bin
        export HADOOP_CONF_DIR=/home/ubuntu/hadoop_home/hadoop-2.9.1/etc/hadoop
	```
   
   vi. Load profile in all the instances:
   
        ```bash
        ~/.profile
	```
     
   vii. Change the hadoop-env.sh in $HADOOP_HOME/etc/hadoop in all the instances to add the below line for JAVA_HOME:
          
	  ```bash
	  export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
	  ```
	  
  ### Hadoop configuration for a 8GB(Memory) node cluster:
      
      This is the configuration for T2.large instances.
        
         filename          config key                          value in mb
         
         mapred-site.xml   yarn.app.mapreduce.am.resource.mb    2048
         mapred-site.xml   mapreduce.map.memory.mb              1024
         mapred-site.xml   mapreduce.reduce.memory.mb           1024
         yarn-site.xml     yarn.nodemanager.resource.memory-mb  6144
         yarn-site.xml     yarn.scheduler.maximum-allocation-mb 6144
         yarn-site.xml     yarn.scheduler.minimum-allocation-mb 1024
	 
  ###  Hadoop Namenode Format:
          
	  After configuring the config files,format the namenode:
          
	  ```bash
          hdfs namenode -format  (Format resets the namenode and should only be done once in the lifetime of a cluster)
	  ```
	  
  ### Start the Hadoop deamons:
      
      
        On Namenode:
	
	 ```bash
         $HADOOP_HOME/sbin/hadoop-daemon.sh --config $HADOOP_CONF_DIR --script hdfs start namenode
         $HADOOP_HOME/sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR start resourcemanager
         $HADOOP_HOME/sbin/mr-jobhistory-daemon.sh --config $HADOOP_CONF_DIR start historyserver
	 ```
         The above 3 deamons will run on the Master node.
         
        On DataNode1:
	
	```bash
         $HADOOP_HOME/sbin/hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs start datanode
	 ```
         The datanode process just needs to be started in 1 slave machine DataNode1.This will start the process in another 
         datanodes on its own through ssh connection.
         
        On DataNode1,DataNode2,DataNode3:
	
	 ```bash
         $HADOOP_HOME/sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR start nodemanager
	 ```
         The nodemanager process needs to be started in all the slave machines.
	 
   ### Creating directories in file system:
        
         hadoop fs -mkdir /user/externaltables/testdata
         hadoop fs -copyFromLocal /home/ubuntu/datafiles/testfile /user/externaltables/testdata/testfile
         
   ### Run the map reduce job to test that the containers are getting launched properly in all the nodes:
         
         yarn jar hadoop-mapreduce-examples-2.9.1.jar wordcount /user/externaltables/testdata/testfile /user/logs
	 
   ### Issues and resolution: 
       
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
       
   ## Hive Installation and Configuration in the Hadoop Cluster
   
   ### Introduction:
   
   The following section describes the installation and configuration of Hive on Hadoop cluster.
   
   ### Motivation

 Hive provides a datawarehousing solution on hadoop and can be used to do computation on data which is of relational format.This has
 a great use case in industries where most of the data are in relational format in legacy systems and are archived in disks.
 When there is a business use case to bring such data on a Big Data Platform and do some analytics on them,then Hive comes as the 
 first choice of platform.Hive can run on multiple execution engines and can be integrated with Spark so that the underlying computation   engine is Spark and not mapreduce.This increases the performance of hive queries greatly.Tez is another execution engine of choice  with Hive.Both have an advantage over map reduce that they do the computation of data in memory by creating immutable datasets and do not write the intermediate query outputs to the hdfs like mapreduce.This saves lot of Physical I/O and makes the processing much faster.
 
  ### Software
 
  Hive-2.3,Derby 10.4.2
  
  ### Installation

  i. Download and Install Hive 2.3 which is compatible with Hadoop 2.9 only in the master machine.
      
      ```bash
      wget https://www-us.apache.org/dist/hive/hive-2.3.3/apache-hive-2.3.3-bin.tar.gz -P ~/hive_installation
      ```
   
  ii.Uncompress the tar file in a directory $HIVE_HOME
      
      ```bash
      tar zxvf ~/hive_installation/apache-hive-* -C ~/hive_home
      ```
      
  iii.Set up the env variables in all the .profile and .bashrc of all the servers
      
      ```bash
      export HIVE_HOME=/home/ubuntu/hive_home/apache-hive-2.3.3-bin
      export PATH=$PATH:$HIVE_HOME/bin
      export HIVE_CONF_DIR=/home/ubuntu/hive_home/apache-hive-2.3.3-bin/conf
      export CLASSPATH=$CLASSPATH:/home/ubuntu/hive_home/apache-hive-2.3.3-bin/lib/*:.
      ```
      
   iv.Download and Install derby database for the metadatastore of hive
      
      ```bash
      wget http://archive.apache.org/dist/db/derby/db-derby-10.4.2.0/db-derby-10.4.2.0-bin.tar.gz
      tar zxvf db-derby-10.4.2.0-bin.tar.gz -C ~/derby_home
      ```
      
   v. Set up the env variables for Derby in .bashrc and .profile
      
      ```bash
      export DERBY_HOME=/home/ubuntu/derby_home/db-derby-10.4.2.0-bin
      export PATH=$PATH:$DERBY_HOME/bin
      export CLASSPATH=$CLASSPATH:$DERBY_HOME/lib/derby.jar:$DERBY_HOME/lib/derbytools.jar
      ```
      
   vi. Load profile in all the servers
       
        ```bash
	~/.profile
	```
        
   vii. Initialize the schema in derby in $HIVE_HOME folder.This will create a metastore_db directory which is the database
         directory and will be loaded in memory.
         
         ```bash
	 cd $HIVE_HOME
	 schematool -dbType derby -initSchema
	 ```
	 
   ### Running Hive

     i. Create directories in HDFS for hive tables:
     
        hadoop fs -mkdir /user/externaltables/insurancedata
        hadoop fs -copyFromLocal insurance_datafile /user/externaltables/insurancedata/
        
     ii. Start the hive terminal by typing command hive
         
	 hive
         
     iii. Create an external table in hive pointing to the file in hdfs:
         
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
         
       iv. Run a query in hive to count the number of policies:
       
          select count(policyID) from insurance_data_1;


