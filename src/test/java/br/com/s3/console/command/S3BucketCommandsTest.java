package br.com.s3.console.command;

import br.com.s3.console.context.BucketContext;
import br.com.s3.console.service.PrinterService;
import br.com.s3.console.service.S3BucketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for S3BucketCommands class")
class S3BucketCommandsTest {

    @Mock
    private S3BucketService s3BucketService;

    @Mock
    private BucketContext bucketContext;

    @Mock
    private PrinterService printerService;

    @InjectMocks
    private S3BucketCommands s3BucketCommands;

    private Bucket bucket1;
    private Bucket bucket2;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();
        
        bucket1 = Bucket.builder()
                .name("bucket1")
                .creationDate(now)
                .build();
        
        bucket2 = Bucket.builder()
                .name("bucket2")
                .creationDate(now)
                .build();
    }

    @Test
    @DisplayName("Given available buckets, when listBuckets is called, then it should return formatted list")
    void givenAvailableBuckets_whenListBuckets_thenShouldReturnFormattedList() {
        // given
        when(s3BucketService.listBuckets()).thenReturn(Arrays.asList(bucket1, bucket2));
        when(printerService.formatBucketsTable(any())).thenReturn("Formatted buckets table");

        // when
        String result = s3BucketCommands.listBuckets();

        // then
        assertThat(result).isEqualTo("Formatted buckets table");
        verify(s3BucketService, times(1)).listBuckets();
        verify(printerService, times(1)).formatBucketsTable(any());
    }

    @Test
    @DisplayName("Given no buckets available, when listBuckets is called, then it should return no buckets message")
    void givenNoBuckets_whenListBuckets_thenShouldReturnNoBucketsMessage() {
        // given
        when(s3BucketService.listBuckets()).thenReturn(Collections.emptyList());
        when(printerService.formatBucketsTable(Collections.emptyList())).thenReturn("No buckets available.");

        // when
        String result = s3BucketCommands.listBuckets();

        // then
        assertThat(result).isEqualTo("No buckets available.");
        verify(s3BucketService, times(1)).listBuckets();
        verify(printerService, times(1)).formatBucketsTable(Collections.emptyList());
    }

    @Test
    @DisplayName("Given valid bucket name, when createBucket is called, then it should create the bucket")
    void givenValidBucketName_whenCreateBucket_thenShouldCreateBucket() {
        // given
        String bucketName = "new-bucket";
        when(s3BucketService.bucketExists(bucketName)).thenReturn(false);
        doNothing().when(s3BucketService).createBucket(bucketName);

        // when
        String result = s3BucketCommands.createBucket(bucketName);

        // then
        assertThat(result).isEqualTo("Bucket 'new-bucket' created successfully.");
        verify(s3BucketService, times(1)).bucketExists(bucketName);
        verify(s3BucketService, times(1)).createBucket(bucketName);
    }

    @Test
    @DisplayName("Given existing bucket, when createBucket is called, then it should return error message")
    void givenExistingBucket_whenCreateBucket_thenShouldReturnErrorMessage() {
        // given
        String bucketName = "existing-bucket";
        when(s3BucketService.bucketExists(bucketName)).thenReturn(true);

        // when
        String result = s3BucketCommands.createBucket(bucketName);

        // then
        assertThat(result).isEqualTo("Bucket 'existing-bucket' already exists.");
        verify(s3BucketService, times(1)).bucketExists(bucketName);
        verify(s3BucketService, never()).createBucket(anyString());
    }

    @Test
    @DisplayName("Given error creating bucket, when createBucket is called, then it should return error message")
    void givenErrorCreatingBucket_whenCreateBucket_thenShouldReturnErrorMessage() {
        // given
        String bucketName = "error-bucket";
        when(s3BucketService.bucketExists(bucketName)).thenReturn(false);
        doThrow(new RuntimeException("Error creating bucket")).when(s3BucketService).createBucket(bucketName);

        // when
        String result = s3BucketCommands.createBucket(bucketName);

        // then
        assertThat(result).isEqualTo("Error creating bucket: Error creating bucket");
        verify(s3BucketService, times(1)).bucketExists(bucketName);
        verify(s3BucketService, times(1)).createBucket(bucketName);
    }

    @Test
    @DisplayName("Given valid bucket name, when deleteBucket is called, then it should delete the bucket")
    void givenValidBucketName_whenDeleteBucket_thenShouldDeleteBucket() {
        // given
        String bucketName = "bucket-to-delete";
        when(s3BucketService.bucketExists(bucketName)).thenReturn(true);
        doNothing().when(s3BucketService).deleteBucket(bucketName);

        // when
        String result = s3BucketCommands.deleteBucket(bucketName);

        // then
        assertThat(result).isEqualTo("Bucket 'bucket-to-delete' deleted successfully.");
        verify(s3BucketService, times(1)).bucketExists(bucketName);
        verify(s3BucketService, times(1)).deleteBucket(bucketName);
    }

    @Test
    @DisplayName("Given non-existing bucket, when deleteBucket is called, then it should return error message")
    void givenNonExistingBucket_whenDeleteBucket_thenShouldReturnErrorMessage() {
        // given
        String bucketName = "non-existing-bucket";
        when(s3BucketService.bucketExists(bucketName)).thenReturn(false);

        // when
        String result = s3BucketCommands.deleteBucket(bucketName);

        // then
        assertThat(result).isEqualTo("Bucket 'non-existing-bucket' does not exist.");
        verify(s3BucketService, times(1)).bucketExists(bucketName);
        verify(s3BucketService, never()).deleteBucket(anyString());
    }

    @Test
    @DisplayName("Given error deleting bucket, when deleteBucket is called, then it should return error message")
    void givenErrorDeletingBucket_whenDeleteBucket_thenShouldReturnErrorMessage() {
        // given
        String bucketName = "error-bucket";
        when(s3BucketService.bucketExists(bucketName)).thenReturn(true);
        doThrow(new RuntimeException("Error deleting bucket")).when(s3BucketService).deleteBucket(bucketName);

        // when
        String result = s3BucketCommands.deleteBucket(bucketName);

        // then
        assertThat(result).isEqualTo("Error deleting bucket: Error deleting bucket");
        verify(s3BucketService, times(1)).bucketExists(bucketName);
        verify(s3BucketService, times(1)).deleteBucket(bucketName);
    }

    @Test
    @DisplayName("Given valid bucket name, when useBucket is called, then it should select the bucket")
    void givenValidBucketName_whenUseBucket_thenShouldSelectBucket() {
        // given
        String bucketName = "bucket-to-use";
        when(s3BucketService.bucketExists(bucketName)).thenReturn(true);
        doNothing().when(bucketContext).setCurrentBucket(bucketName);

        // when
        String result = s3BucketCommands.useBucket(bucketName);

        // then
        assertThat(result).isEqualTo("Bucket 'bucket-to-use' selected successfully.");
        verify(s3BucketService, times(1)).bucketExists(bucketName);
        verify(bucketContext, times(1)).setCurrentBucket(bucketName);
    }

    @Test
    @DisplayName("Given non-existing bucket, when useBucket is called, then it should return error message")
    void givenNonExistingBucket_whenUseBucket_thenShouldReturnErrorMessage() {
        // given
        String bucketName = "non-existing-bucket";
        when(s3BucketService.bucketExists(bucketName)).thenReturn(false);

        // when
        String result = s3BucketCommands.useBucket(bucketName);

        // then
        assertThat(result).isEqualTo("Bucket 'non-existing-bucket' does not exist. Use the 'create-bucket' command to create it.");
        verify(s3BucketService, times(1)).bucketExists(bucketName);
        verify(bucketContext, never()).setCurrentBucket(anyString());
    }

    @Test
    @DisplayName("Given error selecting bucket, when useBucket is called, then it should return error message")
    void givenErrorSelectingBucket_whenUseBucket_thenShouldReturnErrorMessage() {
        // given
        String bucketName = "error-bucket";
        when(s3BucketService.bucketExists(bucketName)).thenReturn(true);
        doThrow(new RuntimeException("Error selecting bucket")).when(bucketContext).setCurrentBucket(bucketName);

        // when
        String result = s3BucketCommands.useBucket(bucketName);

        // then
        assertThat(result).isEqualTo("Error selecting bucket: Error selecting bucket");
        verify(s3BucketService, times(1)).bucketExists(bucketName);
        verify(bucketContext, times(1)).setCurrentBucket(bucketName);
    }

    @Test
    @DisplayName("Given selected bucket, when currentBucket is called, then it should show the current bucket")
    void givenSelectedBucket_whenCurrentBucket_thenShouldShowCurrentBucket() {
        // given
        String bucketName = "current-bucket";
        when(bucketContext.hasBucketSelected()).thenReturn(true);
        when(bucketContext.getCurrentBucket()).thenReturn(bucketName);

        // when
        String result = s3BucketCommands.currentBucket();

        // then
        assertThat(result).isEqualTo("Current bucket: current-bucket");
        verify(bucketContext, times(1)).hasBucketSelected();
        verify(bucketContext, times(1)).getCurrentBucket();
    }

    @Test
    @DisplayName("Given no bucket selected, when currentBucket is called, then it should show no bucket message")
    void givenNoBucketSelected_whenCurrentBucket_thenShouldShowNoBucketMessage() {
        // given
        when(bucketContext.hasBucketSelected()).thenReturn(false);

        // when
        String result = s3BucketCommands.currentBucket();

        // then
        assertThat(result).isEqualTo("No bucket selected. Use the 'use-bucket' command to select a bucket.");
        verify(bucketContext, times(1)).hasBucketSelected();
        verify(bucketContext, never()).getCurrentBucket();
    }
}