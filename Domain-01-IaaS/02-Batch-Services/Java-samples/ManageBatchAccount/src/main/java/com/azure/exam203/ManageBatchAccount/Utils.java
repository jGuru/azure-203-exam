package com.azure.exam203.ManageBatchAccount;


import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.ApplicationPackage;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.BatchAccountKeys;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountEncryptionStatus;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.storage.StorageService;

public class Utils 
{
	
	 /**
     * Print resource group info.
     *
     * @param resource a resource group
     */
    public static void print(ResourceGroup resource) {
        StringBuilder info = new StringBuilder();
        info.append("Resource Group: ").append(resource.id())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags());
        System.out.println(info.toString());
    }
	
	/**
     * Print storage account.
     *
     * @param storageAccount a storage account
     */
    public static void print(StorageAccount storageAccount) {
        System.out.println(storageAccount.name()
                + " created @ " + storageAccount.creationTime());

        StringBuilder info = new StringBuilder().append("Storage Account: ").append(storageAccount.id())
                .append("Name: ").append(storageAccount.name())
                .append("\n\tResource group: ").append(storageAccount.resourceGroupName())
                .append("\n\tRegion: ").append(storageAccount.region())
                .append("\n\tSKU: ").append(storageAccount.skuType().name().toString())
                .append("\n\tAccessTier: ").append(storageAccount.accessTier())
                .append("\n\tKind: ").append(storageAccount.kind());

        info.append("\n\tNetwork Rule Configuration: ")
                .append("\n\t\tAllow reading logs from any network: ").append(storageAccount.canReadLogEntriesFromAnyNetwork())
                .append("\n\t\tAllow reading metrics from any network: ").append(storageAccount.canReadMetricsFromAnyNetwork())
                .append("\n\t\tAllow access from all azure services: ").append(storageAccount.canAccessFromAzureServices());

        if (storageAccount.networkSubnetsWithAccess().size() > 0) {
            info.append("\n\t\tNetwork subnets with access: ");
            for (String subnetId : storageAccount.networkSubnetsWithAccess()) {
                info.append("\n\t\t\t").append(subnetId);
            }
        }
        if (storageAccount.ipAddressesWithAccess().size() > 0) {
            info.append("\n\t\tIP addresses with access: ");
            for (String ipAddress : storageAccount.ipAddressesWithAccess()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }
        if (storageAccount.ipAddressRangesWithAccess().size() > 0) {
            info.append("\n\t\tIP address-ranges with access: ");
            for (String ipAddressRange : storageAccount.ipAddressRangesWithAccess()) {
                info.append("\n\t\t\t").append(ipAddressRange);
            }
        }
        info.append("\n\t\tTraffic allowed from only HTTPS: ").append(storageAccount.inner().enableHttpsTrafficOnly());

        info.append("\n\tEncryption status: ");
        for (Map.Entry<StorageService, StorageAccountEncryptionStatus> eStatus : storageAccount.encryptionStatuses().entrySet()) {
            info.append("\n\t\t").append(eStatus.getValue().storageService()).append(": ").append(eStatus.getValue().isEnabled() ? "Enabled" : "Disabled");
        }

        System.out.println(info.toString());
    }


 /**
     * Print storage account keys.
     *
     * @param storageAccountKeys a list of storage account keys
     */
    public static void print(List<StorageAccountKey> storageAccountKeys) {
        for (int i = 0; i < storageAccountKeys.size(); i++) {
            StorageAccountKey storageAccountKey = storageAccountKeys.get(i);
            System.out.println("Key (" + i + ") " + storageAccountKey.keyName() + "="
                    + storageAccountKey.value());
        }
    }
	/**
     * Prints batch account keys.
     *
     * @param batchAccountKeys a list of batch account keys
     */
    public static void print(BatchAccountKeys batchAccountKeys) {
        System.out.println("Primary Key (" + batchAccountKeys.primary() + ") Secondary key = ("
                + batchAccountKeys.secondary() + ")");
    }
	
	/**
     * Prints batch account.
     *
     * @param batchAccount a Batch Account
     */
    public static void print(BatchAccount batchAccount) {
        StringBuilder applicationsOutput = new StringBuilder().append("\n\tapplications: ");

        if (batchAccount.applications().size() > 0) {
            for (Map.Entry<String, Application> applicationEntry : batchAccount.applications().entrySet()) {
                Application application = applicationEntry.getValue();
                StringBuilder applicationPackages = new StringBuilder().append("\n\t\t\tapplicationPackages : ");

                for (Map.Entry<String, ApplicationPackage> applicationPackageEntry : application.applicationPackages().entrySet()) {
                    ApplicationPackage applicationPackage = applicationPackageEntry.getValue();
                    StringBuilder singleApplicationPackage = new StringBuilder().append("\n\t\t\t\tapplicationPackage : " + applicationPackage.name());
                    singleApplicationPackage.append("\n\t\t\t\tapplicationPackageState : " + applicationPackage.state());

                    applicationPackages.append(singleApplicationPackage);
                    singleApplicationPackage.append("\n");
                }

                StringBuilder singleApplication = new StringBuilder().append("\n\t\tapplication: " + application.name());
                singleApplication.append("\n\t\tdisplayName: " + application.displayName());
                singleApplication.append("\n\t\tdefaultVersion: " + application.defaultVersion());
                singleApplication.append(applicationPackages);
                applicationsOutput.append(singleApplication);
                applicationsOutput.append("\n");
            }
        }

        System.out.println(new StringBuilder().append("BatchAccount: ").append(batchAccount.id())
                .append("Name: ").append(batchAccount.name())
                .append("\n\tResource group: ").append(batchAccount.resourceGroupName())
                .append("\n\tRegion: ").append(batchAccount.region())
                .append("\n\tTags: ").append(batchAccount.tags())
                .append("\n\tAccountEndpoint: ").append(batchAccount.accountEndpoint())
                .append("\n\tPoolQuota: ").append(batchAccount.poolQuota())
                .append("\n\tActiveJobAndJobScheduleQuota: ").append(batchAccount.activeJobAndJobScheduleQuota())
                .append("\n\tStorageAccount: ").append(batchAccount.autoStorage() == null ? "No storage account attached" : batchAccount.autoStorage().storageAccountId())
                .append(applicationsOutput)
                .toString());
    }
 /**
     * Creates and returns a randomized name based on the prefix file for use by the sample.
     * @param namePrefix The prefix string to be used in generating the name.
     * @return a random name
     * */
    public static String createRandomName(String namePrefix) {
        return SdkContext.randomResourceName(namePrefix, 30);
    }

}
