# Hadoop,Hive and Spark multi node Cluster set up on Amazon EC2 instances


| github: [:cloud:](https://github.com/cloudmesh-community/fa18-516-29/blob/master/project-paper/report.md)

## ABSTRACT

The goal of this project is to demonstrate the steps needed to set up a 4 node Hadoop Cluster with Spark and Hive on Amazon EC2 instances from the scratch and do comparison between the traditional mapreduce and Distributed Computing through Spark.It details the Hadoop and Spark configurations required for a 4GB and 8GB(memory) node Hadoop cluster for developmental and testing purpose.The objective is to do all the installations and configurations from the scratch.This process will be the same as doing the installation and set up on any 4 unix machines which have a static IP.

## Keywords

Amazon EC2,Hadoop,Spark,Hive

### 1. INTRODUCTION

The project describes what are the minimum configurations required for a multi node Hadoop cluster set-up with Spark and Hive in AWS EC2 instances and how to establish a passwordless ssh connection between the instances.This is applicable for any unix/linux instances which can have a static IP and the same steps need to be followed for establishing a passwordless ssh connection.The recommended amount of memory for an instance is 8GB and atleast 20GB of physical disk space.

### 2. SOFTWARE VERSIONS

Hadoop 2.9.1
Hive 2.3.3
Spark 2.3.2
	
### 3. ARTIFACTS

* Project Proposal
  Project Code

### 4. REFERENCES
