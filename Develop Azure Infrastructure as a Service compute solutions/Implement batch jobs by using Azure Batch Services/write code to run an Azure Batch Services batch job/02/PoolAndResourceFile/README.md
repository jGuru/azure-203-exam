---
services: Batch <br/>
platforms: java
---

#Prerequisite

- Batch account can be created on azure management portal.
- Storage account


#Getting Started with Batch - Create Pool - in Java


  Azure Batch sample for managing pool -
   - Create IaaS pool
   - Associate ApplicationPackageReference which is already created using Azure Portal with name mytestjavaapp and version 1
   - Associate start task to install java on each virtual machine joining the pool.
   - Wait the VMs to be ready
   - Submit a simple job with task associated with resource file
     - Upload file to Azure storage
     - Generate the SAS url for the file
     - Associate the resource with task
     - Execute ApplicationPackageReference and printing hello world
   - Wait the task to finish
   - Delete the job and the pool (based on the boolean value provided in the code)
 

## Running this Sample

To run this sample:

Set the following environment variables or you just hard code in the code for brevity I have hard coded the sample values
- `AZURE_BATCH_ACCOUNT` -- The Batch account name.
- `AZURE_BATCH_ACCESS_KEY` -- The Batch account key.
- `AZURE_BATCH_ENDPOINT` -- The Batch account endpoint.
- `STORAGE_ACCOUNT_NAME` -- The storage account to hold resource files.
- `STORAGE_ACCOUNT_KEY` -- The storage account key.

##How to get details of batch account to run this example



Clone repo and compile the code:

         mvn clean compile exec:java

#Linux Distribution Support Matrix (Note, this is not an exhaustive list and may subject to change.)

| Distro       | Publisher | Offer          | SKU         | NodeAgentSKUId           |
|--------------|-----------|----------------|-------------|--------------------------|
| Ubuntu       | Canonical | UbuntuServer   | 14.04.0-LTS | batch.node.ubuntu 14.04  |
|              |           |                | 14.04.1-LTS | batch.node.ubuntu 14.04  |
|              |           |                | 14.04.2-LTS | batch.node.ubuntu 14.04  |
|              |           |                | 14.04.3-LTS | batch.node.ubuntu 14.04  |
|              |           |                | 14.04.4-LTS | batch.node.ubuntu 14.04  |
|              |           |                | 15.1        | batch.node.debian 8      |
| Debian       | Credativ  | Debian         | 8           | batch.node.debian 8      |
| SUSE         | SUSE      | openSUSE       | 13.2        | batch.node.opensuse 13.2 |
|              |           | openSUSE-Leap  | 42.1        | batch.node.opensuse 42.1 |
|              |           | SLES           | 12          | batch.node.opensuse 42.1 |
|              |           | SLES           | 12-SP1      | batch.node.opensuse 42.1 |
|              |           | SLES-HPC       | 12          | batch.node.opensuse 42.1 |
| CentOS       | OpenLogic | CentOS         | 7           | batch.node.centos 7      |
|              |           |                | 7.1         | batch.node.centos 7      |
|              |           |                | 7.2         | batch.node.centos 7      |
| Oracle Linux | Oracle    | Oracle-Linux-7 | OL70        | batch.node.centos 7      |







