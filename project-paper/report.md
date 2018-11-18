# Hadoop,Hive and Spark multi node Cluster set up on Amazon EC2 instances


| github: [:cloud:](https://github.com/cloudmesh-community/fa18-516-29/blob/master/project-paper/report.md)

## ABSTRACT

The goal of this project is to demonstrate the steps needed to set up a 4 node Hadoop Cluster with Spark and Hive on Amazon EC2 instances from the scratch.It details the hadoop and spark configurations required for a 4GB and 8GB(memory) node Hadoop cluster for developmental and testing purpose.The objective is to do all the installations and configurations from the scratch.This process will be the same as doing the installation and set up on any 4 unix machines which have a static IP.

## Keywords

Multi-cloud data service, Cloud Computing, Python, Open API, Cloud Providers

### 1. INTRODUCTION

The objective of this project is to manage data across different cloud providers. We are going to build an Open API for managing the data between all the cloud storages. We will analyse how these different clouds work and then build python methods to handle data across them. Final step will be to expose these functionalities as an API.

### 2. IMPLEMENTATION DETAILS

OpenAPI specification for copy function
	Boolean isFileCopied = copyFile({name of the file}, {source cloud provider}, {destination cloud provider}) 
	
Similarly we can have API for other functionalities as well. We can also split verification into a separate API because this might be commonly used across all functionalities.


### 3. ARTIFACTS

* Project Proposal

### 4. REFERENCES
