package com.azure.exam203.ManageBatchAccount;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.batch.BatchClient;
import com.microsoft.azure.batch.DetailLevel;
import com.microsoft.azure.batch.auth.BatchSharedKeyCredentials;
import com.microsoft.azure.batch.protocol.models.AllocationState;
import com.microsoft.azure.batch.protocol.models.ApplicationPackageReference;
import com.microsoft.azure.batch.protocol.models.AutoUserSpecification;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudPool;
import com.microsoft.azure.batch.protocol.models.ComputeNode;
import com.microsoft.azure.batch.protocol.models.ElevationLevel;
import com.microsoft.azure.batch.protocol.models.ImageInformation;
import com.microsoft.azure.batch.protocol.models.ImageReference;
import com.microsoft.azure.batch.protocol.models.OSType;
import com.microsoft.azure.batch.protocol.models.PoolAddParameter;
import com.microsoft.azure.batch.protocol.models.PoolInformation;
import com.microsoft.azure.batch.protocol.models.StartTask;
import com.microsoft.azure.batch.protocol.models.UserIdentity;
import com.microsoft.azure.batch.protocol.models.VerificationType;
import com.microsoft.azure.batch.protocol.models.VirtualMachineConfiguration;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.ApplicationPackage;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.implementation.ApplicationPackageImpl;
import com.microsoft.azure.management.batch.implementation.ApplicationPackageInner;
import com.microsoft.azure.management.batch.implementation.ApplicationsInner;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import com.microsoft.rest.LogLevel;

public class ManageBatchAccount {
	private Azure azure;
	private Region region;
	private StorageAccount storageAccount;
	private ResourceGroup resourceGroup;
	private BatchAccount batchAccount;
	private CloudPool cloudPool;
	private CloudBlobContainer cloudBlobContainer;
	private BatchClient client;
	
	
	public Azure getAzure() {
		return azure;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public StorageAccount getStorageAccount() {
		return storageAccount;
	}

	public ResourceGroup getResourceGroup() {
		return resourceGroup;
	}

	public BatchAccount getBatchAccount() {
		return batchAccount;
	}

	public void login(String authFile, String resourceGroupName) throws CloudException, IOException {
		final File credFile = new File(authFile);
		azure = Azure.configure().withLogLevel(LogLevel.BASIC).authenticate(credFile).withDefaultSubscription();
		
		//Another method to authenticate
		//ApplicationTokenCredentials credentials = new ApplicationTokenCredentials("49841514-97d2-4f87-8b51-193b11bfb44c", "bf95ca1d-cd8e-41fc-a3a0-9710e3bb83da", "hn-0aPRE@.vZ3KqkFZPhw1D6-?NhCVFp", AzureEnvironment.AZURE);
		//this.azure = Azure.authenticate(credentials).withDefaultSubscription();
		resourceGroup=azure.resourceGroups().getByName(resourceGroupName);
	}

	public void createResourceGroup(String resourceGroupName) {
		resourceGroup = azure.resourceGroups().define(resourceGroupName).withRegion(region).create();
	}

	public void createStorageAccount(String storageAccountName) {
		storageAccount = azure.storageAccounts().define(storageAccountName).withRegion(region)
				.withExistingResourceGroup(resourceGroup).create();
	}

	public void createBatchAccount(String batchAccountName) {
		batchAccount = azure.batchAccounts().define(batchAccountName).withRegion(region)
				.withExistingResourceGroup(resourceGroup).withExistingStorageAccount(storageAccount).create();
	}

	public void listBatchAccounts() {

		System.out.println("Listing Batch accounts");
		List<BatchAccount> accounts = azure.batchAccounts().listByResourceGroup(resourceGroup.name());
		BatchAccount ba;
		for (int i = 0; i < accounts.size(); i++) {
			ba = accounts.get(i);
			System.out.println("Batch Account (" + i + ") " + ba.name());
		}
	}

	public BatchAccount getBatchAccountById(String id) {
		return azure.batchAccounts().getById(id);
	}

	public BatchAccount getBatchAccountInResourceGroup(String batchAccountName) {
		return azure.batchAccounts().getByResourceGroup(resourceGroup.name(), batchAccountName);
	}

	public void deleteStorageAccount() {
		azure.storageAccounts().deleteById(storageAccount.id());
	}

	public void deleteBatchAccount() {
		System.out.println("Deleting a batch account - " + batchAccount.name());
		for (Map.Entry<String, Application> applicationEntry : batchAccount.applications().entrySet()) {
			for (Map.Entry<String, ApplicationPackage> applicationPackageEntry : applicationEntry.getValue()
					.applicationPackages().entrySet()) {
				System.out.println("Deleting a application package - " + applicationPackageEntry.getKey());
				applicationPackageEntry.getValue().delete();
			}
			System.out.println("Deleting a application - " + applicationEntry.getKey());
			batchAccount.update().withoutApplication(applicationEntry.getKey()).apply();
		}
		azure.batchAccounts().deleteById(batchAccount.id());
		System.out.println("Deleted batch account");
	}
	
	public void addApplicationPackageReference(String applicationId, String version, File applicationZipFile, boolean allowUpdate,String displayName)
	{
		
		batchAccount.update().defineNewApplication(applicationId).defineNewApplicationPackage(version).withAllowUpdates(allowUpdate).withDisplayName(displayName).attach().apply().refresh();
		
	}
	
	public void createPoolIfNotExists(String poolId, String osPublisher, String osOffer, String poolVMSize, int poolVMCount ) throws BatchErrorException, IllegalArgumentException, IOException, InterruptedException, TimeoutException {
		
		//Adding https:// to end point as method .accountEndpoint method return URL without scheme to be used 
		BatchSharedKeyCredentials cred = new BatchSharedKeyCredentials("https://"+batchAccount.accountEndpoint().toString(),batchAccount.name().toString() ,batchAccount.getKeys().primary());
		client = BatchClient.open(cred);
		
		// Create a pool with 1 A1 VM
		// osPublisher = "OpenLogic";
		// osOffer = "CentOS";
		// poolVMSize = "STANDARD_A1";
		//poolVMCount = 2;
		Duration POOL_STEADY_TIMEOUT = Duration.ofMinutes(5);
		Duration VM_READY_TIMEOUT = Duration.ofMinutes(20);

		// Check if pool exists
		if (!client.poolOperations().existsPool(poolId)) {

			// See detail of creating IaaS pool at
			// https://blogs.technet.microsoft.com/windowshpc/2016/03/29/introducing-linux-support-on-azure-batch/
			// Get the sku image reference
			List<ImageInformation> skus = client.accountOperations().listSupportedImages();
			String skuId = null;
			ImageReference imageRef = null;

			for (ImageInformation sku : skus) {
				if (sku.osType() == OSType.LINUX) {
					if (sku.verificationType() == VerificationType.VERIFIED) {
						if (sku.imageReference().publisher().equalsIgnoreCase(osPublisher)
								&& sku.imageReference().offer().equalsIgnoreCase(osOffer)) {
							imageRef = sku.imageReference();
							skuId = sku.nodeAgentSKUId();
							break;
						}
					}
				}
			}

			// Use IaaS VM with Linux
			VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
			configuration.withNodeAgentSKUId(skuId).withImageReference(imageRef);

			UserIdentity ud = new UserIdentity().withAutoUser(new AutoUserSpecification().withElevationLevel(ElevationLevel.ADMIN));
			StartTask st = new StartTask().withCommandLine("sudo yum install java-1.8.0-openjdk -y").withWaitForSuccess(true).withMaxTaskRetryCount(2).withUserIdentity(ud);
			//List<ApplicationPackageReference> list = new ArrayList<ApplicationPackageReference>();
			//list.add(new ApplicationPackageReference().withApplicationId("mytestjavaapp").withVersion("1"));

			client.poolOperations().createPool(new PoolAddParameter().withId(poolId).withVmSize(poolVMSize).withVirtualMachineConfiguration(configuration).withTargetDedicatedNodes(poolVMCount).withStartTask(st));//.withApplicationPackageReferences(list));
		}

		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;
		boolean steady = false;

		// Wait for the VM to be allocated
		while (elapsedTime < POOL_STEADY_TIMEOUT.toMillis()) {
			CloudPool pool = client.poolOperations().getPool(poolId);
			if (pool.allocationState() == AllocationState.STEADY) {
				steady = true;
				break;
			}
			System.out.println("wait 30 seconds for pool steady...");
			Thread.sleep(30 * 1000);
			elapsedTime = (new Date()).getTime() - startTime;
		}

		if (!steady) {
			throw new TimeoutException("The pool did not reach a steady state in the allotted time");
		}

		// The VMs in the pool don't need to be in and IDLE state in order to submit a
		// job.
		// The following code is just an example of how to poll for the VM state
		startTime = System.currentTimeMillis();
		elapsedTime = 0L;
		boolean hasIdleVM = false;

		// Wait for at least 1 VM to reach the IDLE state
		while (elapsedTime < VM_READY_TIMEOUT.toMillis()) {
			List<ComputeNode> nodeCollection = client.computeNodeOperations().listComputeNodes(poolId,new DetailLevel.Builder().withSelectClause("id, state").withFilterClause("state eq 'idle'").build());
			
			if (!nodeCollection.isEmpty()) {
				hasIdleVM = true;
				break;
			}

			System.out.println("wait 30 seconds for VM start...");
			Thread.sleep(30 * 1000);
			elapsedTime = (new Date()).getTime() - startTime;
		}

		if (!hasIdleVM) {
			throw new TimeoutException("The node did not reach an IDLE state in the allotted time");
		}

		this.cloudPool= client.poolOperations().getPool(poolId);
	}
	
	public void createBlobContainer(String containerName) throws URISyntaxException, StorageException {
		
		// Create storage credential from name and key
		StorageCredentials credentials = new StorageCredentialsAccountAndKey(storageAccount.name().toString(), storageAccount.getKeys().get(0).value().toString());
		
		// Create storage account. The 'true' sets the client to use HTTPS for
		// communication with the account
		CloudStorageAccount storageAccount = new CloudStorageAccount(credentials, true);

		// Create the blob client
		CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

		// Get a reference to a container.
		// The container name must be lower case
		cloudBlobContainer= blobClient.getContainerReference(containerName);
		cloudBlobContainer.createIfNotExists();
		
	}

	
	/**
	 * Upload file to blob container and return sas key
	 * 
	 * @param container blob container
	 * @param fileName  the file name of blob
	 * @param filePath  the local file path
	 * @return SAS key for the uploaded file
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 */
	public String uploadFileToCloud(String fileName, String filePath) throws URISyntaxException, IOException, InvalidKeyException, StorageException {
		// Create the container if it does not exist.
		//container.createIfNotExists();
		
		// Upload file
		CloudBlockBlob blob = cloudBlobContainer.getBlockBlobReference(fileName);
		File source = new File(filePath);
		blob.upload(new FileInputStream(source), source.length());

		// Create policy with 1 day read permission
		SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
		EnumSet<SharedAccessBlobPermissions> perEnumSet = EnumSet.of(SharedAccessBlobPermissions.READ);
		policy.setPermissions(perEnumSet);

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, 1);
		policy.setSharedAccessExpiryTime(c.getTime());

		// Create SAS key
		String sas = blob.generateSharedAccessSignature(policy, null);
		return blob.getUri() + "?" + sas;
	}
	
	public void createJob(String poolId, String jobId) throws BatchErrorException, IOException, StorageException, InvalidKeyException, URISyntaxException {
		
		// Create job run at the specified pool
		PoolInformation poolInfo = new PoolInformation();
		poolInfo.withPoolId(poolId);
		
		//Create Job
		client.jobOperations().createJob(jobId, poolInfo);
	}

	
	
}
