# Hadoop 4-node cluster set up on AWS EC2 instances-Part 1(Instance configuration and passwordless ssh set up)


| github: [:cloud:](https://github.com/cloudmesh-community/fa18-516-29/blob/master/project-paper/report.md)

## Motivation
Before setting up a Hadoop cluster,it is important to set up connection between our client machine and the EC2 instances as 
well as passwordless ssh communication between the EC2 instances.Also,the IP of EC2 instances changes whenever we restart the servers.Since,we will need to stop and restart the machines only when we are using them,it is important to set up Elastic IPs for these instances,which do not change on restart else our passwordless ssh connection set up will not work.

## Keywords

Amazon EC2,Hadoop,Elastic IP configuartion

### 1. Memory and Disk Requirements:
In AWS,create an EC2 instance of type T2.Medium by following the guidelines given in EC2 manual of how to create an instance.The T2 Medium instance has 4GB of memory and initially 8GB of physical storage.In order to launch containers in Hadoop and Spark on a yarn cluster,we found that 4GB of memory in the nodes is the mimimum required.If the memory is below this limit,the Application Master on the slave nodes will not start and the jobs will always be in ACCEPTED state.They will never get allocated to the Application master in slave nodes by the Resource Manager running in master node due to lack of resources.In this scenario also we have to specify the memory configurations in yarn-site.xml and mapreduce-site.xml for each process as Hadoop defaults do not work properly if the memory is less than 8GB.The physical storage of EC2 instance is 8GB and everything is mapped to the drive /dev/xvda.There are few challenges here as there are a lot of log files and local intermediate data which gets written in the datanodes when a mapreduce or spark job runs and this space also gets used up quickly.In our experiment,we found that atleast 11GB of physical storage was needed to start the containers but if we switch to T2.large instances then it would be more expensive.So,we chose to add more volume to T2.medium instance in the physical storage.This is feasible as EC2 instances have EBS storage and volume can be added on the fly.

### 2. Configuring Elastic Ips for the instances:
After allocating 4 instances,1 Namenode and 3 DataNodes of type T2.medium,go to the Network and Security section of the EC2 Mangaement console and allocate 4 new elastic IPs from the IPv4 address pool.After this IP is allocated,map this IP to any instance.Repeat this step for the remaining 3 instances.
--> Image

### 3. Configuring ssh connection between the client machine and EC2 instances:
Get the client-keypair.pem file from AWS while creating instances.Use Putty keygen feature to generate .ppk file from the .pem file for an SSH connection.Add .ppk file in SSH Auth session of the all the nodes and save the session information in putty.Now,we can login in the instances with username ubuntu.

### 3. Configuring passwordless ssh communication between the instances:

 i. Create a config file with name config in ~/.ssh folder with the below entries in all the instances including namenode and datanodes.
      Add the below entries in the file:
      
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
	 
   ii. Copy the keyfile received from aws in the ~/.ssh folder of all the instances.
           local machine to  ~/.ssh/client-keypair.pem
            
   iii.Change the permissions of all the file in ~/.ssh folder to 600 of all the servers.This is a requirement for 
           ssh to work correctly.
       
   iv.Go to the ~/.ssh folder of namenode and run the following command.
          sh-keygen -f ~/.ssh/id_rsa -t rsa -P ""
        
	This will create 2 files sshkey_rsa.pub and sshkey_rsa in ~/.ssh folder. 
        
   v. Copy the contents of sshkey_rsa.pub to authorized_keys file in ~/.ssh folder of namenode by the below command.
           cat ~/.ssh/sshkey_rsa.pub >> ~/.ssh/authorized_keys
           This will create an entry for the user ubuntu in the authorized keys.
           
   vi. Copy the file authorized_keys to all the datanodes instances in .ssh folder.
        
   vii. From the datanode1 and namenode,do ssh to all the other datanodes.It will ask to enter the host names to known_hosts file.
   Confirm yes.
        
   viii. In the /etc/hosts file of all the instances add the following:
        
              172.31.21.154 ec2-52-24-204-101.us-west-2.compute.amazonaws.com
              172.31.16.132 ec2-52-38-172-19.us-west-2.compute.amazonaws.com
              172.31.19.37 ec2-52-42-185-237.us-west-2.compute.amazonaws.com
              172.31.30.216 ec2-52-89-22-141.us-west-2.compute.amazonaws.com
           where the first IP is the Private IP and second is the Public DNS (IPv4) in EC2 management console.
         
   ix. Also change the hostname in all the instances to the public DNS name because by default they are the private IPs.
         
              sudo hostname ec2-52-24-204-101.us-west-2.compute.amazonaws.com.
	      
              This is very important because the instances know each other by their public DNS names.
              Repeat this on all the instances.
        
	The above steps complete the set up of passwordless ssh connection between all the instances.

