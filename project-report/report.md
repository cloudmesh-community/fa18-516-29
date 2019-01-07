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


### Motivation
Before setting up a Hadoop cluster,it is important to set up connection between our client machine and the EC2 instances as 
well as passwordless ssh communication between the EC2 instances.Also,the IP of EC2 instances changes whenever we restart the servers.Since,we will need to stop and restart the machines only when we are using them,it is important to set up Elastic IPs for these instances,which do not change on restart else our passwordless ssh connection set up will not work.


### Cluster Planning:
We will set up a 4 node Hadoop cluster with 1 node as master and 3 as slaves.Each instance is T2.large type and has 8 GB of memory
and initially 16GB of physical storage.This configuration is fine for doing the initial cluster set up and running some map reduce jobs for testing purpose,but definitely will not work for large datasets.For a substantial amount of data transfer,one should go for T2.Xlarge instances which have 16GB of memory and initially 32 GB of physical storage.The only downside is that it will be expensive to own 4 such large instances in EC2 for a developer.


### Memory and Disk Requirements

In AWS,create 4 EC2 instances of type T2.large by following the guidelines given in EC2 manual of how to create an instance.The T2 large instance has 8GB of RAM.In order to launch containers in Hadoop and Spark on a yarn cluster,we found that 4GB of memory in the nodes is the mimimum required.If the memory is below this limit,the Application Master on the slave nodes will not start and the jobs will always be in ACCEPTED state.They will never get allocated to the Application master in slave nodes by the Resource Manager running in master node due to lack of resources.In this scenario also we have to specify the memory configurations in yarn-site.xml and mapreduce-site.xml for each process as Hadoop defaults do not work properly if the memory is less than 8GB.So, we chose to start with T2.large instances where the memory is 8GB.The physical storage of EC2 instances is 16GB and everything is mapped to the drive /dev/xvda.There are few challenges here as there are a lot of log files and local intermediate data which gets written in the datanodes when a mapreduce or spark job runs and this space also gets used up quickly.In our experiment,we found that atleast 11GB of physical storage was needed to start the containers which can get used up as more and more map reduce job runs.So,we chose to add more volume to T2.large instance in the physical storage whenever the space was getting full.This is feasible as EC2 instances have EBS storage and volume can be added on the fly.

### Configuring Elastic Ips for the instances

After allocating 4 instances,1 Namenode and 3 DataNodes of type T2.large,go to the Network and Security section of the EC2 Management console and allocate 4 new Elastic IPs from the IPv4 address pool.After this IP is allocated,map these IPs to the instances.This is important as the IPs in cloud are dynamic and change whenever we stop our machines.So,the set up will not work when we restart the instances again.Inorder to avoid this issue,we first allocate Elastic Ips to our instances.

![4 node cluster on AWS EC2](images/EC2_instances.png){#fig:1 Namenode and 3 Datanodes in AWS EC2}


![AWS EC2 Elastic IP](images/Elastic_IPs.png){#fig:Elastic Ip allocation for AWS EC2 instances}


### Configuring ssh connection between the client machine and EC2 instances

The first step after creating EC2 ubuntu instances is to arrange for login through putty.For this,get the client-keypair.pem file from AWS while creating instances.Use Putty keygen feature to generate .ppk file from the .pem file for an SSH connection.Add .ppk file in SSH Auth session of the all the nodes and save the session information in putty.Now,we can login in the instances with username ubuntu.

### Configuring passwordless ssh communication between the instances

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

