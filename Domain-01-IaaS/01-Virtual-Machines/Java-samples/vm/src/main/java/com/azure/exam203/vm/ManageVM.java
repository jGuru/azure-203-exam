package com.azure.exam203.vm;
import java.io.File;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySetSkuTypes;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.DiskSkuTypes;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.LogLevel;

public class ManageVM {
	
	private Azure azure;
	
	public Azure login(String authfile)
	{
		try {
		    final File credFile = new File(authfile);
		     azure = Azure.configure()
		        .withLogLevel(LogLevel.BASIC)
		        .authenticate(credFile)
		        .withDefaultSubscription();
		    
		} catch (Exception e) {
		    System.out.println(e.getMessage());
		    e.printStackTrace();
		}
		 return azure;
	}
	
	public ResourceGroup createResurceGroup(String rgName, Region region)
	{
		System.out.println("Creating resource group...");
		ResourceGroup resourceGroup = azure.resourceGroups()
		    .define(rgName)
		    .withRegion(region)
		    .create();
		return resourceGroup;
	}
	
	public AvailabilitySet createAvailibilitySet(String availibilitySet, ResourceGroup rg, Region region)
	{
		System.out.println("Creating availability set...");
		AvailabilitySet availabilitySet = azure.availabilitySets()
		    .define(availibilitySet)
		    .withRegion(region)
		    .withExistingResourceGroup(rg)
		    .withSku(AvailabilitySetSkuTypes.MANAGED)
		    .create();
		return availabilitySet;
	}
	
	public PublicIPAddress createPublicIPAddress(String myPublicIPName, ResourceGroup rg, Region region)
	{
		System.out.println("Creating public IP address...");
		PublicIPAddress publicIPAddress = azure.publicIPAddresses()
		    .define(myPublicIPName)
		    .withRegion(region)
		    .withExistingResourceGroup(rg)
		    .withDynamicIP()
		    .create();
		return publicIPAddress;
	}
	
	public Network createVirtualNetwork(String myVNet, ResourceGroup rg,Region region)
	{
		System.out.println("Creating virtual network...");
		Network network = azure.networks()
		    .define(myVNet)
		    .withRegion(region)
		    .withExistingResourceGroup(rg)
		    .withAddressSpace("10.0.0.0/16")
		    .withSubnet("mySubnet","10.0.0.0/24")
		    .create();
		return network;
	}
	
	
	public NetworkInterface createNetworkInterfaceCard(String nicName, Network network, ResourceGroup rg, PublicIPAddress ipAddress, String subnetName, Region region )
	{
		System.out.println("Creating network interface...");
		NetworkInterface networkInterface = azure.networkInterfaces()
		    .define(nicName)
		    .withRegion(region)
		    .withExistingResourceGroup(rg)
		    .withExistingPrimaryNetwork(network)
		    .withSubnet(subnetName)
		    .withPrimaryPrivateIPAddressDynamic()
		    .withExistingPrimaryPublicIPAddress(ipAddress)
		    .create();
		
		return networkInterface;
	}
	
	public VirtualMachine createVirtualMachine(String vmName, Region region, ResourceGroup rg, NetworkInterface networkInerface, String publisher, String offer, String sku,String size, String adminName, String adminPassword, AvailabilitySet availabilitySet)
	{
		System.out.println("Creating virtual machine...");
		VirtualMachine virtualMachine = azure.virtualMachines()
		    .define(vmName)
		    .withRegion(region)
		    .withExistingResourceGroup(rg)
		    .withExistingPrimaryNetworkInterface(networkInerface)
		    .withLatestWindowsImage(publisher, offer, sku) //you can give Linux image as well 
		    .withAdminUsername(adminName)
		    .withAdminPassword(adminPassword)
		    .withComputerName(vmName)
		    .withExistingAvailabilitySet(availabilitySet)
		    .withSize(size)
		    .create();
		return virtualMachine;
	}
	//If you want to use an existing disk instead of a marketplace image, use this code:
	/*
	 * public void createVirtualMachineFromCustomImage(NetworkInterface
	 * networkInterface, AvailabilitySet availabilitySet) { Disk disk =
	 * azure.disks().define("myosdisk") .withRegion(Region.US_EAST)
	 * .withExistingResourceGroup("myResourceGroup") .withWindowsFromVhd(
	 * "https://mystorage.blob.core.windows.net/vhds/myosdisk.vhd")
	 * .withSizeInGB(128) .withSku(DiskSkuTypes.STANDARD_LRS) .create();
	 * 
	 * azure.virtualMachines().define("myVM") .withRegion(Region.US_EAST)
	 * .withExistingResourceGroup("myResourceGroup")
	 * .withExistingPrimaryNetworkInterface(networkInterface)
	 * .withSpecializedOSDisk(disk, OperatingSystemTypes.WINDOWS)
	 * .withExistingAvailabilitySet(availabilitySet)
	 * .withSize(VirtualMachineSizeTypes.STANDARD_DS1) .create(); }
	 */
	
	public void stopVirtualMachine(VirtualMachine vm)
	{
		System.out.println("Stopping vm...");
		vm.powerOff();
	}
	
	public void deallocateVirtualMachine(VirtualMachine vm)
	{
		System.out.println("Deallocating vm...");
		vm.deallocate();
	}
	
	public void startVirtualMachine(VirtualMachine vm)
	{
		System.out.println("Starting vm...");
		vm.start();
	}
	
	public void restartVirtualMachine(VirtualMachine vm)
	{
		System.out.println("Restarting vm...");
		vm.restart();
	}
	
	public void resizeVirtualMachine(VirtualMachine vm)
	{
		System.out.println("Resizing vm...");
		vm.update()
		    .withSize(VirtualMachineSizeTypes.STANDARD_DS2)
		    .apply();
	}
	
	public void addDataDiskToVirtualMachine(VirtualMachine vm)
	{
		System.out.println("Adding data disk...");
		vm.update()
		    .withNewDataDisk(2, 0, CachingTypes.READ_WRITE)
		    .apply();
	}
	
}
