package saleson.service.s3storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;

@Service
public class S3ServiceImpl
{

	protected static final Logger LOGGER = LoggerFactory.getLogger(S3ServiceImpl.class);

	private AmazonS3 s3Client;

	private TransferManager transferManager;

	@Value("${cloud.aws.s3.bucket_name}")
	private String bucketName;

	@Value("${cloud.aws.credentials.access_key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret_key}")
	private String secretKey;

	@Value("${cloud.aws.region}")
	private String region;

	private Map<String, List<Upload>> uploadProgressStores = new HashMap<>();

	@PostConstruct
	private void initializeAmazon()
	{
		BasicAWSCredentials creds = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3Client = AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(new AWSStaticCredentialsProvider(creds))
				.build();
		transferManager = TransferManagerBuilder.standard()
				.withS3Client(s3Client)
				.withMultipartUploadThreshold((long) (5 * 1024 * 1025))
				.build();
	}

	public String uploadFile(String requestId, String folderName, String fileName, boolean forceReceiveFolderUrl){
		File file = new File("C:\\itleadpro\\mms\\src\\main\\resources\\static\\test.html");
		try{
//            file = getFileInSession(requestId, fileName);
			String fileUrl = uploadFileTos3bucket(folderName, fileName, file);
			System.out.println("Upload file: " + fileUrl);
			if(forceReceiveFolderUrl){
				fileUrl = s3Client.getUrl(bucketName, String.format("%s/", folderName)).toURI().toASCIIString();
			}
			return fileUrl;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public void cancelUpload(String folderName)
	{
		// Cancel uploading items
		List<Upload> uploadings = uploadProgressStores.get(folderName);
		if (uploadings != null)
		{
			uploadings.stream().forEach(item -> {
				if (!item.isDone())
				{
					item.abort();
				}
			});
		}

		// Remove folder of uploaded items
		DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucketName, folderName);
		s3Client.deleteObject(deleteRequest);
	}

	private void storeUploader(String folderName, Upload uploader)
	{
		List<Upload> uploads = new ArrayList<>();
		if (uploadProgressStores.containsKey(folderName) && uploadProgressStores.get(folderName) != null)
		{
			uploads.addAll(uploadProgressStores.get(folderName));
		}

		uploads.add(uploader);

		uploadProgressStores.put(folderName, uploads);
	}

//    private File convertStreamSourceToFile(String fileName, InputStream source) throws IOException
//    {
//        File convFile = new File(fileName);
//        FileOutputStream fos = new FileOutputStream(convFile);
//        IOUtils.copy(source, fos);
//        IOUtils.closeQuietly(fos);
//        return convFile;
//    }

	private String generateFileName(String originalFileName)
	{
		return new Date().getTime() + "-" + originalFileName.replace(" ", "_");
	}

	private String uploadFileTos3bucket(String folderName, String fileName, File file) throws URISyntaxException
	{
		String key = mergeFileName(folderName, fileName);
		s3Client.putObject(
				new PutObjectRequest(bucketName, key, file).withCannedAcl(CannedAccessControlList.PublicRead));
		return s3Client.getUrl(bucketName, key).toURI().toASCIIString();
	}

	public String uploadFileTos3bucket(String folderName, String fileName, MultipartFile file) throws URISyntaxException, IOException
	{
		String key = mergeFileName(folderName, fileName);

		byte[] byteArr = file.getBytes();
		InputStream inputStream = new ByteArrayInputStream(byteArr);
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(file.getBytes().length);

		s3Client.putObject(
				new PutObjectRequest(bucketName, key, inputStream, objectMetadata).withCannedAcl(CannedAccessControlList.PublicRead));
		return s3Client.getUrl(bucketName, key).toURI().toASCIIString();
	}

	private String mergeFileName(String folderName, String fileName)
	{
		String key = String.format("%s/%s", folderName, fileName);

		return key;
	}
}