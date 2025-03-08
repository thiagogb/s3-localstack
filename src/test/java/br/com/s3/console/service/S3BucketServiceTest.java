package br.com.s3.console.service;

import br.com.s3.console.context.BucketContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for S3BucketService class")
class S3BucketServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private BucketContext bucketContext;

    @InjectMocks
    private S3BucketService s3BucketService;

    private Bucket bucket1;
    private Bucket bucket2;

    @BeforeEach
    void setUp() {
        bucket1 = Bucket.builder()
                .name("bucket1")
                .creationDate(Instant.now())
                .build();
        
        bucket2 = Bucket.builder()
                .name("bucket2")
                .creationDate(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Given configured S3 client, when listBuckets is called, then it should return the list of buckets")
    void givenS3Client_whenListBuckets_thenShouldReturnBucketsList() {
        // given
        ListBucketsResponse response = ListBucketsResponse.builder()
                .buckets(Arrays.asList(bucket1, bucket2))
                .build();
        when(s3Client.listBuckets()).thenReturn(response);

        // when
        List<Bucket> result = s3BucketService.listBuckets();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(bucket1, bucket2);
        verify(s3Client, times(1)).listBuckets();
    }

    @Test
    @DisplayName("Given S3 error, when listBuckets is called, then it should throw RuntimeException")
    void givenS3Error_whenListBuckets_thenShouldThrowRuntimeException() {
        // given
        when(s3Client.listBuckets()).thenThrow(S3Exception.builder().message("S3 error").build());

        // when/then
        assertThatThrownBy(() -> s3BucketService.listBuckets())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error listing buckets");
        
        verify(s3Client, times(1)).listBuckets();
    }

    @Test
    @DisplayName("Given valid bucket name, when createBucket is called, then it should create the bucket")
    void givenValidBucketName_whenCreateBucket_thenShouldCreateBucket() {
        // given
        String bucketName = "new-bucket";
        when(s3Client.createBucket(any(CreateBucketRequest.class))).thenReturn(
                CreateBucketResponse.builder().build());

        // when
        s3BucketService.createBucket(bucketName);

        // then
        verify(s3Client, times(1)).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    @DisplayName("Given S3 error, when createBucket is called, then it should throw RuntimeException")
    void givenS3Error_whenCreateBucket_thenShouldThrowRuntimeException() {
        // given
        String bucketName = "error-bucket";
        when(s3Client.createBucket(any(CreateBucketRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        // when/then
        assertThatThrownBy(() -> s3BucketService.createBucket(bucketName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error creating bucket");
        
        verify(s3Client, times(1)).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    @DisplayName("Given empty bucket, when deleteBucket is called, then it should delete the bucket")
    void givenEmptyBucket_whenDeleteBucket_thenShouldDeleteBucket() {
        // given
        String bucketName = "empty-bucket";
        
        // Mock bucket is empty
        ListObjectsV2Response emptyResponse = ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(emptyResponse);
        
        // Mock delete bucket
        when(s3Client.deleteBucket(any(DeleteBucketRequest.class))).thenReturn(
                DeleteBucketResponse.builder().build());
        
        // when
        s3BucketService.deleteBucket(bucketName);

        // then
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, times(1)).deleteBucket(any(DeleteBucketRequest.class));
    }

    @Test
    @DisplayName("Given current empty bucket, when deleteBucket is called, then it should delete the bucket and clear the context")
    void givenCurrentEmptyBucket_whenDeleteBucket_thenShouldDeleteBucketAndClearContext() {
        // given
        String bucketName = "current-bucket";
        
        // Mock bucket is empty
        ListObjectsV2Response emptyResponse = ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(emptyResponse);
        
        // Mock delete bucket
        when(s3Client.deleteBucket(any(DeleteBucketRequest.class))).thenReturn(
                DeleteBucketResponse.builder().build());
        
        // Mock current bucket
        when(bucketContext.getCurrentBucket()).thenReturn(bucketName);
        
        // when
        s3BucketService.deleteBucket(bucketName);

        // then
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, times(1)).deleteBucket(any(DeleteBucketRequest.class));
        verify(bucketContext, atLeastOnce()).getCurrentBucket();
        verify(bucketContext, times(1)).clearCurrentBucket();
    }

    @Test
    @DisplayName("Given non-empty bucket, when deleteBucket is called, then it should throw RuntimeException")
    void givenNonEmptyBucket_whenDeleteBucket_thenShouldThrowRuntimeException() {
        // given
        String bucketName = "non-empty-bucket";
        
        // Mock bucket is not empty
        S3Object object = S3Object.builder().key("file.txt").build();
        ListObjectsV2Response nonEmptyResponse = ListObjectsV2Response.builder()
                .contents(Collections.singletonList(object))
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(nonEmptyResponse);
        
        // when/then
        assertThatThrownBy(() -> s3BucketService.deleteBucket(bucketName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("The bucket is not empty. Empty it before deleting.");
        
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, never()).deleteBucket(any(DeleteBucketRequest.class));
    }

    @Test
    @DisplayName("Given S3 error, when deleteBucket is called, then it should throw RuntimeException")
    void givenS3Error_whenDeleteBucket_thenShouldThrowRuntimeException() {
        // given
        String bucketName = "error-bucket";
        
        // Mock bucket is empty
        ListObjectsV2Response emptyResponse = ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(emptyResponse);
        
        // Mock delete bucket error
        when(s3Client.deleteBucket(any(DeleteBucketRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());
        
        // when/then
        assertThatThrownBy(() -> s3BucketService.deleteBucket(bucketName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error deleting bucket");
        
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, times(1)).deleteBucket(any(DeleteBucketRequest.class));
    }

    @Test
    @DisplayName("Given existing bucket, when bucketExists is called, then it should return true")
    void givenExistingBucket_whenBucketExists_thenShouldReturnTrue() {
        // given
        String bucketName = "existing-bucket";
        
        // Mock head bucket success
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(
                HeadBucketResponse.builder().build());
        
        // when
        boolean result = s3BucketService.bucketExists(bucketName);

        // then
        assertThat(result).isTrue();
        verify(s3Client, times(1)).headBucket(any(HeadBucketRequest.class));
    }

    @Test
    @DisplayName("Given non-existing bucket, when bucketExists is called, then it should return false")
    void givenNonExistingBucket_whenBucketExists_thenShouldReturnFalse() {
        // given
        String bucketName = "non-existing-bucket";
        
        // Mock head bucket not found
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenThrow(
                NoSuchBucketException.builder().message("Bucket not found").build());
        
        // when
        boolean result = s3BucketService.bucketExists(bucketName);

        // then
        assertThat(result).isFalse();
        verify(s3Client, times(1)).headBucket(any(HeadBucketRequest.class));
    }

    @Test
    @DisplayName("Given 404 error in S3, when bucketExists is called, then it should return false")
    void given404Error_whenBucketExists_thenShouldReturnFalse() {
        // given
        String bucketName = "404-bucket";
        
        // Mock head bucket 404 error
        AwsServiceException exception = S3Exception.builder()
                .statusCode(404)
                .message("404 Not Found")
                .build();
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenThrow(exception);
        
        // when
        boolean result = s3BucketService.bucketExists(bucketName);

        // then
        assertThat(result).isFalse();
        verify(s3Client, times(1)).headBucket(any(HeadBucketRequest.class));
    }

    @Test
    @DisplayName("Given other S3 error, when bucketExists is called, then it should propagate the exception")
    void givenOtherS3Error_whenBucketExists_thenShouldPropagateException() {
        // given
        String bucketName = "error-bucket";
        
        // Mock head bucket other error
        AwsServiceException exception = S3Exception.builder()
                .statusCode(500)
                .message("Internal Server Error")
                .build();
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenThrow(exception);
        
        // when/then
        assertThatThrownBy(() -> s3BucketService.bucketExists(bucketName))
                .isInstanceOf(S3Exception.class)
                .hasMessageContaining("Internal Server Error");
        
        verify(s3Client, times(1)).headBucket(any(HeadBucketRequest.class));
    }
} 