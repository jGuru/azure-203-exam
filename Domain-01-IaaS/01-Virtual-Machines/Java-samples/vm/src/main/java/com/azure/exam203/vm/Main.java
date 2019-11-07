package com.azure.exam203.vm;

import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Hello world!
 *
 */
public class Main 
{
    public static void main( String[] args )
    {
    	Region region=Region.US_EAST;
    	ManageVM mvm=new ManageVM();
    	mvm.login("C:\\Source\\Azure\\vm\\src\\main\\java\\azureauth.properties");
    	ResourceGroup rg=mvm.createResurceGroup("myRg2", region);
    	
    	//Availability sets make it easier for you to maintain the virtual machines used by your application.
    	//This is equivalent to availability zone of AWS
    	AvailabilitySet availabilitySet= mvm.createAvailibilitySet("myavailibilityset", rg, region);
    	
    	//A Public IP address is needed to communicate with the virtual machine.
    	PublicIPAddress ipaddress=mvm.createPublicIPAddress("myPublicIP", rg, region);
    	
    	//Create the virtual network
    	//A virtual machine must be in a subnet of a Virtual network this is equivalent to VPN of AWS
    	Network network=mvm.createVirtualNetwork("myvnet",rg,region);
    	
    	//Create the network interface
    	//A virtual machine needs a network interface to communicate on the virtual network.
    	
    	NetworkInterface networkInerface= mvm.createNetworkInterfaceCard("mynic", network, rg, ipaddress, "mysubnet", region);
    	
    	//Create the virtual machine
    	//Now we created all the supporting resources, we can create a virtual machine.
    	//Publisher	                  offer				sku			       size	
    	//"MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter"  Standard_DS1
    	//or a linux image details
    	
    	VirtualMachine vm=mvm.createVirtualMachine("myvm", region, rg, networkInerface, "MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter", "Standard_DS1", "Neeraj", "Neeraj12345678", availabilitySet);
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    }
}
