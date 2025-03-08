package br.com.s3.console.service;

import br.com.s3.console.context.BucketContext;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3BucketFileService {

    private final S3Client s3Client;
    private final BucketContext bucketContext;

    public S3BucketFileService(S3Client s3Client, BucketContext bucketContext) {
        this.s3Client = s3Client;
        this.bucketContext = bucketContext;
    }

    /**
     * Lists all objects in the current S3 bucket
     * @return List of S3 objects
     */
    public List<S3Object> listObjects() {
        checkBucketSelected();
        
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketContext.getCurrentBucket())
                    .build();
            
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.contents();
        } catch (S3Exception e) {
            throw new RuntimeException("Error listing objects from bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Downloads an object from S3
     * @param key Object key
     * @param destinationPath Destination path to save the file
     * @return Path of the downloaded file
     */
    public Path downloadObject(String key, String destinationPath) {
        checkBucketSelected();
        
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketContext.getCurrentBucket())
                    .key(key)
                    .build();
            
            Path filePath = Paths.get(destinationPath, key);
            File file = filePath.toFile();
            
            // Create parent directories if they don't exist
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            Files.copy(response, filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return filePath;
        } catch (Exception e) {
            throw new RuntimeException("Error downloading object: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an object from S3
     * @param key Object key
     */
    public void deleteObject(String key) {
        checkBucketSelected();
        
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketContext.getCurrentBucket())
                    .key(key)
                    .build();
            
            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            throw new RuntimeException("Error deleting object: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads a file to S3
     * @param filePath Local file path
     * @param key Object key in S3
     */
    public void uploadObject(String filePath, String key) {
        checkBucketSelected();
        
        try {
            File file = new File(filePath);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketContext.getCurrentBucket())
                    .key(key)
                    .build();
            
            s3Client.putObject(request, RequestBody.fromFile(file));
        } catch (Exception e) {
            throw new RuntimeException("Error uploading object: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes all objects from the S3 bucket
     * @return Number of objects deleted
     */
    public int deleteAllObjects() {
        checkBucketSelected();
        
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketContext.getCurrentBucket())
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            List<S3Object> objects = listResponse.contents();
            
            if (objects.isEmpty()) {
                return 0;
            }
            
            // Create a list of objects for deletion
            List<ObjectIdentifier> objectsToDelete = objects.stream()
                    .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                    .collect(Collectors.toList());
            
            // Create the batch deletion request
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketContext.getCurrentBucket())
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();
            
            // Execute the batch deletion
            DeleteObjectsResponse deleteResponse = s3Client.deleteObjects(deleteRequest);
            
            return deleteResponse.deleted().size();
        } catch (S3Exception e) {
            throw new RuntimeException("Error clearing bucket: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if a bucket is selected
     * @throws IllegalStateException if no bucket is selected
     */
    private void checkBucketSelected() {
        if (!bucketContext.hasBucketSelected()) {
            throw new IllegalStateException("No bucket selected. Use the 'use-bucket' command to select a bucket.");
        }
    }
} 