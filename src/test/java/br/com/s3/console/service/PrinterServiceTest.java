package br.com.s3.console.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for PrinterService class")
class PrinterServiceTest {

    private PrinterService printerService;
    private Bucket bucket1;
    private Bucket bucket2;
    private S3Object object1;
    private S3Object object2;

    @BeforeEach
    void setUp() {
        printerService = new PrinterService();
        
        Instant now = Instant.now();
        
        bucket1 = Bucket.builder()
                .name("bucket1")
                .creationDate(now)
                .build();
        
        bucket2 = Bucket.builder()
                .name("bucket2")
                .creationDate(now)
                .build();
        
        object1 = S3Object.builder()
                .key("file1.txt")
                .size(100L)
                .lastModified(now)
                .storageClass("STANDARD")
                .build();
        
        object2 = S3Object.builder()
                .key("file2.txt")
                .size(200L)
                .lastModified(now)
                .storageClass("STANDARD")
                .build();
    }

    @Test
    @DisplayName("Given list of buckets, when formatBucketsTable is called, then it should return formatted table")
    void givenBucketsList_whenFormatBucketsTable_thenShouldReturnFormattedTable() {
        // given
        List<Bucket> buckets = Arrays.asList(bucket1, bucket2);

        // when
        String result = printerService.formatBucketsTable(buckets);

        // then
        assertThat(result).isNotBlank();
        assertThat(result).contains("Available buckets:");
        assertThat(result).contains("Name");
        assertThat(result).contains("Creation Date");
        assertThat(result).contains("bucket1");
        assertThat(result).contains("bucket2");
    }

    @Test
    @DisplayName("Given empty list of buckets, when formatBucketsTable is called, then it should return no buckets message")
    void givenEmptyBucketsList_whenFormatBucketsTable_thenShouldReturnNoBucketsMessage() {
        // given
        List<Bucket> buckets = Collections.emptyList();

        // when
        String result = printerService.formatBucketsTable(buckets);

        // then
        assertThat(result).isEqualTo("No buckets available.");
    }

    @Test
    @DisplayName("Given list of objects, when formatObjectsTable is called, then it should return formatted table")
    void givenObjectsList_whenFormatObjectsTable_thenShouldReturnFormattedTable() {
        // given
        List<S3Object> objects = Arrays.asList(object1, object2);
        String bucketName = "test-bucket";

        // when
        String result = printerService.formatObjectsTable(objects, bucketName);

        // then
        assertThat(result).isNotBlank();
        assertThat(result).contains("Files in bucket 'test-bucket':");
        assertThat(result).contains("Name");
        assertThat(result).contains("Size (bytes)");
        assertThat(result).contains("Last Modified");
        assertThat(result).contains("Storage Class");
        assertThat(result).contains("file1.txt");
        assertThat(result).contains("file2.txt");
        assertThat(result).contains("100");
        assertThat(result).contains("200");
        assertThat(result).contains("STANDARD");
    }

    @Test
    @DisplayName("Given empty list of objects, when formatObjectsTable is called, then it should return empty bucket message")
    void givenEmptyObjectsList_whenFormatObjectsTable_thenShouldReturnEmptyBucketMessage() {
        // given
        List<S3Object> objects = Collections.emptyList();
        String bucketName = "empty-bucket";

        // when
        String result = printerService.formatObjectsTable(objects, bucketName);

        // then
        assertThat(result).isEqualTo("Bucket 'empty-bucket' is empty.");
    }
} 