package br.com.s3.console.service;

import br.com.s3.console.context.BucketContext;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

@Service
public class S3BucketService {

    private final S3Client s3Client;
    private final BucketContext bucketContext;

    public S3BucketService(S3Client s3Client, BucketContext bucketContext) {
        this.s3Client = s3Client;
        this.bucketContext = bucketContext;
    }

    /**
     * Lists all available buckets
     * @return List of buckets
     */
    public List<Bucket> listBuckets() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            return response.buckets();
        } catch (S3Exception e) {
            throw new RuntimeException("Error listing buckets: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new bucket
     * @param bucketName Bucket name
     */
    public void createBucket(String bucketName) {
        try {
            CreateBucketRequest request = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.createBucket(request);
        } catch (S3Exception e) {
            throw new RuntimeException("Error creating bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a bucket
     * @param bucketName Bucket name
     */
    public void deleteBucket(String bucketName) {
        try {
            // Check if the bucket is empty
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            if (!listResponse.contents().isEmpty()) {
                throw new RuntimeException("The bucket is not empty. Empty it before deleting.");
            }
            
            DeleteBucketRequest request = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.deleteBucket(request);
            
            // If the deleted bucket is the current one, clear the context
            if (bucketContext.getCurrentBucket() != null && 
                bucketContext.getCurrentBucket().equals(bucketName)) {
                bucketContext.clearCurrentBucket();
            }
        } catch (S3Exception e) {
            throw new RuntimeException("Error deleting bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a bucket exists
     * @param bucketName Bucket name
     * @return true if the bucket exists, false otherwise
     */
    public boolean bucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
} 