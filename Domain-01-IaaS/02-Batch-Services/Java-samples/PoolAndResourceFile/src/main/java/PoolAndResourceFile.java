import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.microsoft.azure.batch.BatchClient;
import com.microsoft.azure.batch.DetailLevel;
import com.microsoft.azure.batch.auth.BatchSharedKeyCredentials;
import com.microsoft.azure.batch.protocol.models.AllocationState;
import com.microsoft.azure.batch.protocol.models.ApplicationPackageReference;
import com.microsoft.azure.batch.protocol.models.AutoUserSpecification;
import com.microsoft.azure.batch.protocol.models.BatchErrorDetail;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudPool;
import com.microsoft.azure.batch.protocol.models.CloudTask;
import com.microsoft.azure.batch.protocol.models.ComputeNode;
import com.microsoft.azure.batch.protocol.models.ElevationLevel;
import com.microsoft.azure.batch.protocol.models.ImageInformation;
import com.microsoft.azure.batch.protocol.models.ImageReference;
import com.microsoft.azure.batch.protocol.models.JobAddParameter;
import com.microsoft.azure.batch.protocol.models.OSType;
import com.microsoft.azure.batch.protocol.models.PoolAddParameter;
import com.microsoft.azure.batch.protocol.models.PoolInformation;
import com.microsoft.azure.batch.protocol.models.ResourceFile;
import com.microsoft.azure.batch.protocol.models.StartTask;
import com.microsoft.azure.batch.protocol.models.TaskAddParameter;
import com.microsoft.azure.batch.protocol.models.TaskState;
import com.microsoft.azure.batch.protocol.models.UserIdentity;
import com.microsoft.azure.batch.protocol.models.VerificationType;
import com.microsoft.azure.batch.protocol.models.VirtualMachineConfiguration;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
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

public class PoolAndResourceFile {

	/**
	 * Create IaaS pool if pool isn't exist
	 * 
	 * @param client batch client instance
	 * @param poolId the pool id
	 * @return the pool instance
	 * @throws BatchErrorException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private static CloudPool createPoolIfNotExists(BatchClient client, String poolId) throws BatchErrorException, IllegalArgumentException, IOException, InterruptedException, TimeoutException {
		// Create a pool with 1 A1 VM
		String osPublisher = "OpenLogic";
		String osOffer = "CentOS";
		String poolVMSize = "STANDARD_A1";
		int poolVMCount = 2;
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
			List<ApplicationPackageReference> list = new ArrayList<ApplicationPackageReference>();
			list.add(new ApplicationPackageReference().withApplicationId("mytestjavaapp").withVersion("1"));

			client.poolOperations().createPool(new PoolAddParameter().withId(poolId).withVmSize(poolVMSize).withVirtualMachineConfiguration(configuration).withTargetDedicatedNodes(poolVMCount).withStartTask(st).withApplicationPackageReferences(list));
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

		return client.poolOperations().getPool(poolId);
	}

	/**
	 * Create blob container in order to upload file
	 * 
	 * @param storageAccountName storage account name
	 * @param storageAccountKey  storage account key
	 * @return CloudBlobContainer instance
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	private static CloudBlobContainer createBlobContainer(String storageAccountName, String storageAccountKey) throws URISyntaxException, StorageException {
		String CONTAINER_NAME = "poolsandresourcefiles";

		// Create storage credential from name and key
		StorageCredentials credentials = new StorageCredentialsAccountAndKey(storageAccountName, storageAccountKey);

		// Create storage account. The 'true' sets the client to use HTTPS for
		// communication with the account
		CloudStorageAccount storageAccount = new CloudStorageAccount(credentials, true);

		// Create the blob client
		CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

		// Get a reference to a container.
		// The container name must be lower case
		return blobClient.getContainerReference(CONTAINER_NAME);
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
	private static String uploadFileToCloud(CloudBlobContainer container, String fileName, String filePath) throws URISyntaxException, IOException, InvalidKeyException, StorageException {
		// Create the container if it does not exist.
		container.createIfNotExists();

		// Upload file
		CloudBlockBlob blob = container.getBlockBlobReference(fileName);
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

	/**
	 * Create a job with a single task
	 * 
	 * @param client    batch client instance
	 * @param container blob container to upload the resource file
	 * @param poolId    pool id
	 * @param jobId     job id
	 * @throws BatchErrorException
	 * @throws IOException
	 * @throws StorageException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 */
	private static void submitJobAndAddTask(BatchClient client, CloudBlobContainer container, String poolId, String jobId) throws BatchErrorException, IOException, StorageException, InvalidKeyException, URISyntaxException {
		
		//Change below hard coded path to your location of the test.txt file or the resouce file that you wanted to upload
		
		String BLOB_FILE_NAME = "test.txt";
		String LOCAL_FILE_PATH = "C:\\Source\\azure-batch-samples\\Java\\PoolAndResourceFile\\" + BLOB_FILE_NAME;

		// Create job run at the specified pool
		PoolInformation poolInfo = new PoolInformation();
		poolInfo.withPoolId(poolId);
		
		//Create Job
		client.jobOperations().createJob(jobId, poolInfo);

		// Create task
		TaskAddParameter taskToAdd = new TaskAddParameter();

		//You can add command line command to act on resource file optional for this example
		
		// taskToAdd.withId("mytask").withCommandLine(String.format("cat %s",BLOB_FILE_NAME));
		
		//Adding existing application package reference created usin Azure Portal it is a list of references max allowed application is 20 while writing this code
		
		List<ApplicationPackageReference> list=new  ArrayList<ApplicationPackageReference>();  
		list.add(new ApplicationPackageReference().withApplicationId("mytestjavaapp").withVersion("1")); 
		  
        taskToAdd.withApplicationPackageReferences(list);
        
        //Sample jar file for applicationpackageReference is can be found in root folder
        
		taskToAdd.withId("mytask").withCommandLine("/bin/sh -c \"java -jar $AZ_BATCH_APP_PACKAGE_mytestjavaapp_1/test.jar\"");

		String sas = uploadFileToCloud(container, BLOB_FILE_NAME, LOCAL_FILE_PATH);

		// Associate resource file with task
		ResourceFile file = new ResourceFile();
		file.withFilePath(BLOB_FILE_NAME).withHttpUrl(sas);
		List<ResourceFile> files = new ArrayList<ResourceFile>();
		files.add(file);
		taskToAdd.withResourceFiles(files);

		// Add task to job
		client.taskOperations().createTask(jobId, taskToAdd);
	}

	/**
	 * Wait all tasks under a specified job to be completed
	 * 
	 * @param client     batch client instance
	 * @param jobId      job id
	 * @param expiryTime the waiting period
	 * @return if task completed in time, return true, otherwise, return false
	 * @throws BatchErrorException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static boolean waitForTasksToComplete(BatchClient client, String jobId, Duration expiryTime) throws BatchErrorException, IOException, InterruptedException 
	{
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;

		while (elapsedTime < expiryTime.toMillis()) {
			List<CloudTask> taskCollection = client.taskOperations().listTasks(jobId, new DetailLevel.Builder().withSelectClause("id, state").build());

			boolean allComplete = true;
			for (CloudTask task : taskCollection) {
				if (task.state() != TaskState.COMPLETED) {
					allComplete = false;
					break;
				}
			}

			if (allComplete) {
				// All tasks completed
				return true;
			}

			System.out.println("wait 10 seconds for tasks to complete...");
			// Check again after 10 seconds
			Thread.sleep(10 * 1000);
			elapsedTime = (new Date()).getTime() - startTime;
		}
		// Timeout, return false
		return false;
	}

	/**
	 * print BatchErrorException to console
	 * 
	 * @param err BatchErrorException instance
	 */
	private static void printBatchException(BatchErrorException err) {
		System.out.println(String.format("BatchError %s", err.toString()));
		if (err.body() != null) {
			System.out.println(String.format("BatchError code = %s, message = %s", err.body().code(),
					err.body().message().value()));
			if (err.body().values() != null) {
				for (BatchErrorDetail detail : err.body().values()) {
					System.out.println(String.format("Detail %s=%s", detail.key(), detail.value()));
				}
			}
		}
	}

	public static void main(String argv[]) throws Exception {
		
		// Get batch and storage account information from environment
		// String batchAccount =System.getenv("AZURE_BATCH_ACCOUNT");
		// String batchKey = System.getenv("AZURE_BATCH_ACCESS_KEY");
		// String batchUri =System.getenv("AZURE_BATCH_ENDPOINT");

		//String storageAccountName = System.getenv("STORAGE_ACCOUNT_NAME");
		//String storageAccountKey =  System.getenv("STORAGE_ACCOUNT_KEY");

		//Hard coded sample values for brevity comment below two lies use env variables and uncomment above 5 lines of code.
		
		String storageAccountName = "mystoreageaccout";
		String storageAccountKey = "LvxlFrUbKq6I4qO559ehDZKGXcLTJscKAcmDDX3w6xERoeu0ABY02CwH4jurYuntxcSa+0vlGAFn8mGOTjkL1w==";

		//Set below values to true if you wanted to delete them after completion
		Boolean shouldDeleteContainer = false;
		Boolean shouldDeleteJob = false;
		Boolean shouldDeletePool = false;
		Duration TASK_COMPLETE_TIMEOUT = Duration.ofMinutes(1);
		String STANDARD_CONSOLE_OUTPUT_FILENAME = "stdout.txt";

		// Create batch client
		//Uncomment below line to use env variable and comment the line after this line which has hard coded values for brevity note these are sample values and do not work
		//BatchSharedKeyCredentials cred = new BatchSharedKeyCredentials(AZURE_BATCH_ENDPOINT, AZURE_BATCH_ACCOUNT,AZURE_BATCH_ACCESS_KEY);
		BatchSharedKeyCredentials cred = new BatchSharedKeyCredentials("https://bab0551834fa30dcc8a3.eastus.batch.azure.com", "bab0551834fa30dcc8a3","sOLquMryNw9JGsYM/NroDfV2VDuwfLccjT23D8D4ux7M3ArFYm5rDYF6vwPVqZtYMRsgZXzmI+clGG8SJRTWyQ==");
		BatchClient client = BatchClient.open(cred);

		// Create storage container
		CloudBlobContainer container = createBlobContainer(storageAccountName, storageAccountKey);

		String userName = System.getProperty("user.name");
		String poolId = userName + "-pooltest";
		String jobId = "HelloWorldJob-" + userName + "-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-');

		try {
			CloudPool sharedPool = createPoolIfNotExists(client, poolId);
			submitJobAndAddTask(client, container, sharedPool.id(), jobId);
			if (waitForTasksToComplete(client, jobId, TASK_COMPLETE_TIMEOUT)) {
				// Get the task command output file
				CloudTask task = client.taskOperations().getTask(jobId, "mytask");

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				client.fileOperations().getFileFromTask(jobId, task.id(), STANDARD_CONSOLE_OUTPUT_FILENAME, stream);
				String fileContent = stream.toString("UTF-8");
				System.out.println(fileContent);
			} else {
				throw new TimeoutException("Task did not complete within the specified timeout");
			}
		} catch (BatchErrorException err) {
			printBatchException(err);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// Clean up the resource if necessary
			if (shouldDeleteJob) {
				try {
					client.jobOperations().deleteJob(jobId);
				} catch (BatchErrorException err) {
					printBatchException(err);
				}
			}

			if (shouldDeletePool) {
				try {
					client.jobOperations().deleteJob(poolId);
				} catch (BatchErrorException err) {
					printBatchException(err);
				}
			}

			if (shouldDeleteContainer) {
				container.deleteIfExists();
			}
		}
	}
}
