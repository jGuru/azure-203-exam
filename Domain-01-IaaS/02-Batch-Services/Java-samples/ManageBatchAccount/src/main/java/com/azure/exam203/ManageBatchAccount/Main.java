package com.azure.exam203.ManageBatchAccount;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class Main {

	public static void main(String[] args) throws Exception {
		// Login to your azure account with default credentials

		ManageBatchAccount manageBatchAccount = new ManageBatchAccount();
		manageBatchAccount.login("C:\\Source\\Azure\\ManageBatchAccount\\src\\main\\java\\azureauth.properties","myRG");
		manageBatchAccount.setRegion(Region.US_EAST);

		// Create Storage Account
		System.out.println("Creating Storage Account");
		manageBatchAccount.createStorageAccount("mystorageaccount2509");
		Thread.sleep(1000);
		
		// Creating blob container
		System.out.println("Creating Blob Container");
		manageBatchAccount.createBlobContainer("mycontainer2509");
		Thread.sleep(100);

		//Uploading file to blob container
		System.out.println("Uploading sample resource file");
		manageBatchAccount.uploadFileToCloud("test.txt", "C:\\Source\\Azure\\ManageBatchAccount\\test.txt");
		
		// Create BachAccount
		System.out.println("Creating Batch Account");
		manageBatchAccount.createBatchAccount("mybatchaccount2509");
		Thread.sleep(1000);
		System.out.println("Uploading sample resource file");
				
		System.out.println("Creating pool");
		manageBatchAccount.createPoolIfNotExists("mypool", "OpenLogic", "CentOS", "STANDARD_A1", 1);
				
		System.out.println("Creating Job");
		manageBatchAccount.createJob("mypool", "myjob");
		
		
		// Print BatchAccount
		Utils.print(manageBatchAccount.getBatchAccount());
		Utils.print(manageBatchAccount.getStorageAccount());
	

		// Delete BatchAccount
		// manageBatchAccount.deleteBatchAccount();

		// Delete StorageAccount
		// manageBatchAccount.deleteStorageAccount();

	}

}
