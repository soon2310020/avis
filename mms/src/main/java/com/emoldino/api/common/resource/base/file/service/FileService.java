package com.emoldino.api.common.resource.base.file.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.emoldino.api.common.resource.base.file.dto.FilePostTmpOut;
import com.emoldino.api.common.resource.base.file.enumeration.StorageType;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;

@Service
public class FileService {
	@Value("${app.fileService.storageType:s3}")
	private String storageType;

	@Value("${cloud.aws.region}")
	private String region;

	@Value("${cloud.aws.s3.bucket_name}")
	private String bucketName;

	@Value("${cloud.aws.credentials.access_key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret_key}")
	private String secretKey;

	private AmazonS3 client;

	@PostConstruct
	private void initialize() {
		BasicAWSCredentials creds = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.client = AmazonS3ClientBuilder.standard()//
				.withRegion(region)//
				.withCredentials(new AWSStaticCredentialsProvider(creds))//
				.build();
	}

	public String newFileLocation(String id) {
//		String customerCode = ValueUtils.toString(BeanUtils.get(UserService.class).getCustomerServer(), "none");
		String dir = DateUtils2.format(DateUtils2.getInstant(), "yyyy/MMdd/", Zone.GMT);
//		String id = UUID.randomUUID().toString();
		String value = dir + id;
		return value;
	}

	public void move(String tmpId, String fileLocation) {
		LogicUtils.assertNotEmpty(tmpId, "tmpId");

		String key = toTmpKey(tmpId);
		String toKey = getCustomerCode() + "/" + fileLocation;

		if (storageType.equalsIgnoreCase(StorageType.S3.name())) {
			InputStream is = null;
			try {
				S3Object object = client.getObject(bucketName, key);
				is = object.getObjectContent();

				ObjectMetadata options = new ObjectMetadata();
				client.putObject(bucketName, toKey, is, options);
			} catch (Exception e) {
				AbstractException ae = ValueUtils.toAe(e, null);
				throw ae;
			} finally {
				ValueUtils.closeQuietly(is);
			}
		}
	}

	public void remove(String fileLocation) {
		LogicUtils.assertNotEmpty(fileLocation, "fileLocation");

		if (storageType.equalsIgnoreCase(StorageType.S3.name())) {
			InputStream is = null;
			try {
				String key = getCustomerCode() + "/" + fileLocation;
				String toKey = getCustomerCode() + "/removed/" + fileLocation;

				S3Object object = client.getObject(bucketName, key);
				is = object.getObjectContent();

				ObjectMetadata options = new ObjectMetadata();
				client.putObject(bucketName, toKey, is, options);
				client.deleteObject(bucketName, key);
			} catch (Exception e) {
				AbstractException ae = ValueUtils.toAe(e, null);
				throw ae;
			} finally {
				ValueUtils.closeQuietly(is);
			}
		}
	}

	public void delete(String fileLocation) {
		LogicUtils.assertNotEmpty(fileLocation, "fileLocation");

		if (storageType.equalsIgnoreCase(StorageType.S3.name())) {
			InputStream is = null;
			try {
				String key = getCustomerCode() + "/removed/" + fileLocation;
				client.deleteObject(bucketName, key);
			} catch (Exception e) {
				AbstractException ae = ValueUtils.toAe(e, null);
				throw ae;
			} finally {
				ValueUtils.closeQuietly(is);
			}
		}
	}

	public boolean existsTmp(String id) {
		ValueUtils.assertNotEmpty(id, "id");

		String key = toTmpKey(id);

		// 1. Copy S3 File -> Local File
		if (storageType.equalsIgnoreCase(StorageType.S3.name())) {
			return client.doesObjectExist(bucketName, key);
		}

		// 2. Local File -> File Object
		Path path = getSysTmpdirPath().resolve(key);
		return Files.exists(path);
	}

	public long getTmpSize(String id) {
		String key = toTmpKey(id);
		S3Object object = client.getObject(bucketName, key);
		long size = object.getObjectMetadata().getContentLength();
		return size;
	}

	/**
	 * Get File Temporary
	 * @param id
	 * @return
	 */
	public File getTmp(String id) {
		ValueUtils.assertNotEmpty(id, "id");

		String key = toTmpKey(id);

		File file = getByKey(key);
		return file;
	}

	public InputStream getTmpInputStream(String id) {
		ValueUtils.assertNotEmpty(id, "id");

		String key = toTmpKey(id);

		S3Object object = client.getObject(bucketName, key);
		InputStream is = object.getObjectContent();
		return is;
	}

	public InputStream getInputStreamByFileLocation(String fileLocation) {
		String key = getCustomerCode() + "/" + fileLocation;
		S3Object object = client.getObject(bucketName, key);
		InputStream is = object.getObjectContent();
		return is;
	}

	public File getByFileLocation(String fileLocation) {
		if (ObjectUtils.isEmpty(fileLocation)) {
			return null;
		}
		String key = getCustomerCode() + "/" + fileLocation;
		File file = getByKey(key);
		return file;
	}

	private File getByKey(String key) {
		// 1. Copy S3 File -> Local File
		if (storageType.equalsIgnoreCase(StorageType.S3.name())) {
			InputStream is = null;
			try {
				S3Object object = client.getObject(bucketName, key);
				is = object.getObjectContent();
				Path path = getSysTmpdirPath().resolve(key);
				int index = key.lastIndexOf("/");
				if (index > 0) {
					String dir = key.substring(0, index);
					Files.createDirectories(getSysTmpdirPath().resolve(dir));
				}
				Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				AbstractException ae = ValueUtils.toAe(e, null);
				throw ae;
			} finally {
				ValueUtils.closeQuietly(is);
			}
		}

		// 2. Local File -> File Object
		Path path = getSysTmpdirPath().resolve(key);
		return path.toFile();
	}

//	public InputStream getFileInputStreamByFileLocation(String fileLocation) {
//		String toKey = getCustomerCode() + "/" + fileLocation;
//		return getFileInputStreamByFileKey(toKey);
//	}

//	public InputStream getFileInputStreamByFileKey(String key) {
//		ValueUtils.assertNotEmpty(key, "key");
//		// 1. Copy S3 File -> Local File
//		if (storageType.equalsIgnoreCase(StorageType.S3.name())) {
//			InputStream is = null;
//			try {
//				S3Object object = client.getObject(bucketName, key);
//				is = object.getObjectContent();
//				return is;
//			} catch (Exception e) {
//				AbstractException ae = ValueUtils.toAe(e, null);
//				throw ae;
//			} finally {
////				ValueUtils.closeQuietly(is);
//			}
//		}
//
//		// 2. Local File -> File Object
//		Path path = getSysTmpdirPath().resolve(key);
//		try {
//			return Files.exists(path) ? Files.newInputStream(path) : null;
//		} catch (IOException e) {
//			AbstractException ae = ValueUtils.toAe(e, null);
//			throw ae;
//		}
//	}

	/**
	 * Post File Temporary
	 * @param file
	 * @return
	 */
	public FilePostTmpOut postTmp(MultipartFile file) {
		LogicUtils.assertNotNull(file, "file");
		try {
			FilePostTmpOut output = postTmp(file.getInputStream());
			return output;
		} catch (Exception e) {
			AbstractException ae = ValueUtils.toAe(e, null);
			throw ae;
		}
	}

	public FilePostTmpOut postTmp(InputStream is) {
		LogicUtils.assertNotNull(is, "is");

		try {
			String id = UUID.randomUUID().toString();
			String key = toTmpKey(id);

			// 1. S3 Storage Case
			// Create S3 File with 6 Hours Expiration Date
			if (storageType.equalsIgnoreCase(StorageType.S3.name())) {
				ObjectMetadata options = new ObjectMetadata();
				Date expDate = new Date(DateUtils2.newInstant().plus(Duration.ofHours(6)).toEpochMilli());
				options.setHttpExpiresDate(expDate);
				client.putObject(bucketName, key, is, options);
			}
			// 2. Local Storage Case
			// TODO Remove Using Local Storage
			else {
				Files.createDirectories(getSysTmpdirPath());
				Path path = getSysTmpdirPath().resolve(key);
				Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
			}

			return new FilePostTmpOut(id);

		} catch (Exception e) {
			AbstractException ae = ValueUtils.toAe(e, null);
			throw ae;
		} finally {
			ValueUtils.closeQuietly(is);
		}
	}

	/**
	 * Delete File Temporary
	 * @param id
	 */
	public void deleteTmp(String id) {
		String key = toTmpKey(id);

		// 1. Delete Local File
		Path path = getSysTmpdirPath().resolve(key);
		try {
			Files.deleteIfExists(path);
		} catch (Exception e) {
			// DO Nothing
		}

		// 2. Delete S3 File
		if (storageType.equalsIgnoreCase(StorageType.S3.name())) {
			ValueUtils.assertNotEmpty(id, "id");
			client.deleteObject(bucketName, key);
		}
	}

	/**
	 * TODO Clean Expired Temporal Files Batch
	 */
	public void cleanTmpBatch() {

	}

	/**
	 * Path of java.io.tmpdir
	 * @return
	 */
	private static Path getSysTmpdirPath() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		Path path = Paths.get(tmpdir).toAbsolutePath();
		return path;
	}

	/**
	 * File ID -> Temporal File Key<br>
	 * tmpKey: {customerCode}/tmp/{fileId}
	 * @param id	File ID
	 * @return
	 */
	private static String toTmpKey(String id) {
		if (ObjectUtils.isEmpty(id)) {
			return null;
		}
		if (id.contains("/")) {
			id = id.substring(id.lastIndexOf("/") + 1);
		}
		if (ObjectUtils.isEmpty(id)) {
			return null;
		}
		String key = getTmpPathStr() + "/" + id;
		return key;
	}

	private static String getTmpPathStr() {
		String path = getCustomerCode() + "/tmp";
		return path;
	}

	private static String getCustomerCode() {
		String customerCode = ValueUtils.toString(ConfigUtils.getServerName(), "none");
		return customerCode;
	}

}
