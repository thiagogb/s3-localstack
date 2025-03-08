package br.com.s3.console.service;

import br.com.s3.console.context.BucketContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for S3BucketFileService class")
class S3BucketFileServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private BucketContext bucketContext;

    @InjectMocks
    private S3BucketFileService s3BucketFileService;

    @TempDir
    Path tempDir;

    private S3Object object1;
    private S3Object object2;
    private String bucketName;

    @BeforeEach
    void setUp() {
        bucketName = "test-bucket";
        
        object1 = S3Object.builder()
                .key("file1.txt")
                .size(100L)
                .lastModified(Instant.now())
                .storageClass("STANDARD")
                .build();
        
        object2 = S3Object.builder()
                .key("file2.txt")
                .size(200L)
                .lastModified(Instant.now())
                .storageClass("STANDARD")
                .build();
        
        lenient().when(bucketContext.getCurrentBucket()).thenReturn(bucketName);
        lenient().when(bucketContext.hasBucketSelected()).thenReturn(true);
    }

    @Test
    @DisplayName("Given selected bucket, when listObjects is called, then it should return the list of objects")
    void givenSelectedBucket_whenListObjects_thenShouldReturnObjectsList() {
        // given
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(Arrays.asList(object1, object2))
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        // when
        List<S3Object> result = s3BucketFileService.listObjects();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(object1, object2);
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    @DisplayName("Given no bucket selected, when listObjects is called, then it should throw IllegalStateException")
    void givenNoBucketSelected_whenListObjects_thenShouldThrowIllegalStateException() {
        // given
        when(bucketContext.hasBucketSelected()).thenReturn(false);

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.listObjects())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No bucket selected");
        
        verify(s3Client, never()).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    @DisplayName("Given S3 error, when listObjects is called, then it should throw RuntimeException")
    void givenS3Error_whenListObjects_thenShouldThrowRuntimeException() {
        // given
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.listObjects())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error listing objects from bucket");
        
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    @DisplayName("Given selected bucket and existing object, when downloadObject is called, then it should download the object")
    void givenSelectedBucketAndExistingObject_whenDownloadObject_thenShouldDownloadObject() throws Exception {
        // given
        String key = "file.txt";
        String content = "file content";
        
        // Mock da resposta do S3
        ResponseInputStream<GetObjectResponse> responseStream = mock(ResponseInputStream.class);
        lenient().when(responseStream.readAllBytes()).thenReturn(content.getBytes());
        lenient().when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        // when
        Path result = s3BucketFileService.downloadObject(key, tempDir.toString());

        // then
        assertThat(result).exists();
        assertThat(result.getFileName().toString()).isEqualTo(key);
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("Given no bucket selected, when downloadObject is called, then it should throw IllegalStateException")
    void givenNoBucketSelected_whenDownloadObject_thenShouldThrowIllegalStateException() {
        // given
        when(bucketContext.hasBucketSelected()).thenReturn(false);

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.downloadObject("file.txt", tempDir.toString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No bucket selected");
        
        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("Given S3 error, when downloadObject is called, then it should throw RuntimeException")
    void givenS3Error_whenDownloadObject_thenShouldThrowRuntimeException() {
        // given
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.downloadObject("file.txt", tempDir.toString()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error downloading object");
        
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("Given selected bucket, when deleteObject is called, then it should delete the object")
    void givenSelectedBucket_whenDeleteObject_thenShouldDeleteObject() {
        // given
        String key = "file.txt";
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        // when
        s3BucketFileService.deleteObject(key);

        // then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Given no bucket selected, when deleteObject is called, then it should throw IllegalStateException")
    void givenNoBucketSelected_whenDeleteObject_thenShouldThrowIllegalStateException() {
        // given
        when(bucketContext.hasBucketSelected()).thenReturn(false);

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.deleteObject("file.txt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No bucket selected");
        
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Given S3 error, when deleteObject is called, then it should throw RuntimeException")
    void givenS3Error_whenDeleteObject_thenShouldThrowRuntimeException() {
        // given
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.deleteObject("file.txt"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error deleting object");
        
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Given selected bucket and existing file, when uploadObject is called, then it should upload the file")
    void givenSelectedBucketAndExistingFile_whenUploadObject_thenShouldUploadFile() throws Exception {
        // given
        String key = "file.txt";
        File tempFile = tempDir.resolve("temp.txt").toFile();
        tempFile.createNewFile();
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // when
        s3BucketFileService.uploadObject(tempFile.getAbsolutePath(), key);

        // then
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Given no bucket selected, when uploadObject is called, then it should throw IllegalStateException")
    void givenNoBucketSelected_whenUploadObject_thenShouldThrowIllegalStateException() {
        // given
        when(bucketContext.hasBucketSelected()).thenReturn(false);

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.uploadObject("path/to/file.txt", "file.txt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No bucket selected");
        
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Given S3 error, when uploadObject is called, then it should throw RuntimeException")
    void givenS3Error_whenUploadObject_thenShouldThrowRuntimeException() throws Exception {
        // given
        File tempFile = tempDir.resolve("temp.txt").toFile();
        tempFile.createNewFile();
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.uploadObject(tempFile.getAbsolutePath(), "file.txt"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error uploading object");
        
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Given selected bucket with objects, when deleteAllObjects is called, then it should delete all objects")
    void givenSelectedBucketWithObjects_whenDeleteAllObjects_thenShouldDeleteAllObjects() {
        // given
        ListObjectsV2Response listResponse = ListObjectsV2Response.builder()
                .contents(Arrays.asList(object1, object2))
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResponse);
        
        DeleteObjectsResponse deleteResponse = DeleteObjectsResponse.builder()
                .deleted(Arrays.asList(
                        DeletedObject.builder().key(object1.key()).build(),
                        DeletedObject.builder().key(object2.key()).build()
                ))
                .build();
        when(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(deleteResponse);

        // when
        int result = s3BucketFileService.deleteAllObjects();

        // then
        assertThat(result).isEqualTo(2);
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, times(1)).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    @DisplayName("Given selected empty bucket, when deleteAllObjects is called, then it should return zero")
    void givenSelectedEmptyBucket_whenDeleteAllObjects_thenShouldReturnZero() {
        // given
        ListObjectsV2Response listResponse = ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResponse);

        // when
        int result = s3BucketFileService.deleteAllObjects();

        // then
        assertThat(result).isZero();
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    @DisplayName("Given no bucket selected, when deleteAllObjects is called, then it should throw IllegalStateException")
    void givenNoBucketSelected_whenDeleteAllObjects_thenShouldThrowIllegalStateException() {
        // given
        when(bucketContext.hasBucketSelected()).thenReturn(false);

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.deleteAllObjects())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No bucket selected");
        
        verify(s3Client, never()).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    @DisplayName("Given S3 error, when deleteAllObjects is called, then it should throw RuntimeException")
    void givenS3Error_whenDeleteAllObjects_thenShouldThrowRuntimeException() {
        // given
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        // when/then
        assertThatThrownBy(() -> s3BucketFileService.deleteAllObjects())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error clearing bucket");
        
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }
} 