package br.com.s3.console.command;

import br.com.s3.console.context.BucketContext;
import br.com.s3.console.service.PrinterService;
import br.com.s3.console.service.S3BucketFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for S3BucketFileCommands class")
class S3BucketFileCommandsTest {

    @Mock
    private S3BucketFileService s3BucketFileService;

    @Mock
    private BucketContext bucketContext;

    @Mock
    private PrinterService printerService;

    @InjectMocks
    private S3BucketFileCommands s3BucketFileCommands;

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
    }

    @Test
    @DisplayName("Given selected bucket with objects, when listFiles is called, then it should return formatted list")
    void givenSelectedBucketWithObjects_whenListFiles_thenShouldReturnFormattedList() {
        // given
        when(s3BucketFileService.listObjects()).thenReturn(Arrays.asList(object1, object2));
        when(printerService.formatObjectsTable(any(), eq(bucketName))).thenReturn("Formatted objects table");

        // when
        String result = s3BucketFileCommands.listFiles();

        // then
        assertThat(result).isEqualTo("Formatted objects table");
        verify(s3BucketFileService, times(1)).listObjects();
        verify(printerService, times(1)).formatObjectsTable(any(), eq(bucketName));
    }

    @Test
    @DisplayName("Given error listing files, when listFiles is called, then it should return error message")
    void givenErrorListingFiles_whenListFiles_thenShouldReturnErrorMessage() {
        // given
        when(s3BucketFileService.listObjects()).thenThrow(new IllegalStateException("No bucket selected"));

        // when
        String result = s3BucketFileCommands.listFiles();

        // then
        assertThat(result).isEqualTo("No bucket selected");
    }

    @Test
    @DisplayName("Given selected bucket and valid key, when downloadFile is called, then it should download the file")
    void givenSelectedBucketAndValidKey_whenDownloadFile_thenShouldDownloadFile() {
        // given
        String key = "file.txt";
        String destination = tempDir.toString();
        Path downloadedPath = tempDir.resolve(key);
        
        when(s3BucketFileService.downloadObject(key, destination)).thenReturn(downloadedPath);

        // when
        String result = s3BucketFileCommands.downloadFile(key, destination);

        // then
        assertThat(result).isEqualTo("File downloaded successfully: " + downloadedPath);
        verify(s3BucketFileService, times(1)).downloadObject(key, destination);
    }

    @Test
    @DisplayName("Given error downloading file, when downloadFile is called, then it should return error message")
    void givenErrorDownloadingFile_whenDownloadFile_thenShouldReturnErrorMessage() {
        // given
        String key = "file.txt";
        String destination = tempDir.toString();
        when(s3BucketFileService.downloadObject(anyString(), anyString()))
            .thenThrow(new IllegalStateException("No bucket selected"));

        // when
        String result = s3BucketFileCommands.downloadFile(key, destination);

        // then
        assertThat(result).isEqualTo("No bucket selected");
    }

    @Test
    @DisplayName("Given selected bucket and valid key, when deleteFile is called, then it should delete the file")
    void givenSelectedBucketAndValidKey_whenDeleteFile_thenShouldDeleteFile() {
        // given
        String key = "file.txt";
        doNothing().when(s3BucketFileService).deleteObject(key);

        // when
        String result = s3BucketFileCommands.deleteFile(key);

        // then
        assertThat(result).isEqualTo("File '" + key + "' deleted successfully from bucket '" + bucketName + "'");
        verify(s3BucketFileService, times(1)).deleteObject(key);
    }

    @Test
    @DisplayName("Given error deleting file, when deleteFile is called, then it should return error message")
    void givenErrorDeletingFile_whenDeleteFile_thenShouldReturnErrorMessage() {
        // given
        String key = "file.txt";
        doThrow(new IllegalStateException("No bucket selected"))
            .when(s3BucketFileService).deleteObject(key);

        // when
        String result = s3BucketFileCommands.deleteFile(key);

        // then
        assertThat(result).isEqualTo("No bucket selected");
    }

    @Test
    @DisplayName("Given selected bucket and valid file, when uploadFile is called, then it should upload the file")
    void givenSelectedBucketAndValidFile_whenUploadFile_thenShouldUploadFile() {
        // given
        String filePath = tempDir.resolve("file.txt").toString();
        String key = "file.txt";
        doNothing().when(s3BucketFileService).uploadObject(filePath, key);

        // when
        String result = s3BucketFileCommands.uploadFile(filePath, key);

        // then
        assertThat(result).isEqualTo("File '" + filePath + "' uploaded successfully to bucket '" + bucketName + "' with key '" + key + "'");
        verify(s3BucketFileService, times(1)).uploadObject(filePath, key);
    }

    @Test
    @DisplayName("Given error uploading file, when uploadFile is called, then it should return error message")
    void givenErrorUploadingFile_whenUploadFile_thenShouldReturnErrorMessage() {
        // given
        String filePath = tempDir.resolve("file.txt").toString();
        String key = "file.txt";
        doThrow(new IllegalStateException("No bucket selected"))
            .when(s3BucketFileService).uploadObject(filePath, key);

        // when
        String result = s3BucketFileCommands.uploadFile(filePath, key);

        // then
        assertThat(result).isEqualTo("No bucket selected");
    }

    @Test
    @DisplayName("Given selected bucket with objects, when clearBucket is called, then it should clear the bucket")
    void givenSelectedBucketWithObjects_whenClearBucket_thenShouldClearBucket() {
        // given
        when(s3BucketFileService.deleteAllObjects()).thenReturn(2);

        // when
        String result = s3BucketFileCommands.clearBucket();

        // then
        assertThat(result).isEqualTo("2 file(s) deleted from bucket '" + bucketName + "'.");
        verify(s3BucketFileService, times(1)).deleteAllObjects();
    }

    @Test
    @DisplayName("Given selected empty bucket, when clearBucket is called, then it should return already empty message")
    void givenSelectedEmptyBucket_whenClearBucket_thenShouldReturnAlreadyEmptyMessage() {
        // given
        when(s3BucketFileService.deleteAllObjects()).thenReturn(0);

        // when
        String result = s3BucketFileCommands.clearBucket();

        // then
        assertThat(result).isEqualTo("Bucket '" + bucketName + "' is already empty.");
        verify(s3BucketFileService, times(1)).deleteAllObjects();
    }

    @Test
    @DisplayName("Given error clearing bucket, when clearBucket is called, then it should return error message")
    void givenErrorClearingBucket_whenClearBucket_thenShouldReturnErrorMessage() {
        // given
        when(s3BucketFileService.deleteAllObjects())
            .thenThrow(new IllegalStateException("No bucket selected"));

        // when
        String result = s3BucketFileCommands.clearBucket();

        // then
        assertThat(result).isEqualTo("No bucket selected");
    }
}