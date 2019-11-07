#!/bin/bash
#Note while writing to this script azure cli has a bug and if you run this script directly then it wont work but if you copy paste command and run them on shell it will work
AdminPassword=Neeraj12345678
az network vnet create -g myRg2 --name MyVnet --address-prefix "10.0.0.0/16" --subnet-name mySubnet --subnet-prefix "10.0.0.0/24"
#
# Create a public IP address.
#
 az network public-ip create --resource-group myRg2 --name myPublicIP
#
# Create a network security group.
#
az network nsg create --resource-group myRg2 --name myNetworkSecurityGroup
#
# Create a virtual network card and associate with public IP address and NSG.
#
az network nic create -g myRg2 --vnet-name MyVnet --subnet mySubnet -n MyNic --location eastus --network-security-group myNetworkSecurityGroup --public-ip-address myPublicIP 
#
# Create a virtual machine. 
#
az vm create --resource-group myRg2 --name myVM --location eastus --nics myNic --image win2016datacenter --admin-username azureuser --admin-password $AdminPassword
#
# Open port 3389 to allow RDP traffic to host.
#
az vm open-port --port 3389 --resource-group myRg2 --name myVM
#
#Cleaning up deployment
#
#az group delete --name myRg2 --yes