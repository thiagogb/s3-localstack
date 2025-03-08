package br.com.s3.console.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for BucketContext class")
class BucketContextTest {

    @Test
    @DisplayName("Given a bucket name, when setCurrentBucket is called, then the bucket should be stored")
    void givenBucketName_whenSetCurrentBucket_thenBucketShouldBeStored() {
        // given
        BucketContext bucketContext = new BucketContext();
        String bucketName = "test-bucket";

        // when
        bucketContext.setCurrentBucket(bucketName);

        // then
        assertThat(bucketContext.getCurrentBucket()).isEqualTo(bucketName);
    }

    @Test
    @DisplayName("Given a selected bucket, when getCurrentBucket is called, then it should return the bucket name")
    void givenSelectedBucket_whenGetCurrentBucket_thenShouldReturnBucketName() {
        // given
        BucketContext bucketContext = new BucketContext();
        String bucketName = "test-bucket";
        bucketContext.setCurrentBucket(bucketName);

        // when
        String result = bucketContext.getCurrentBucket();

        // then
        assertThat(result).isEqualTo(bucketName);
    }

    @Test
    @DisplayName("Given no bucket selected, when getCurrentBucket is called, then it should return null")
    void givenNoBucketSelected_whenGetCurrentBucket_thenShouldReturnNull() {
        // given
        BucketContext bucketContext = new BucketContext();

        // when
        String result = bucketContext.getCurrentBucket();

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Given a selected bucket, when hasBucketSelected is called, then it should return true")
    void givenSelectedBucket_whenHasBucketSelected_thenShouldReturnTrue() {
        // given
        BucketContext bucketContext = new BucketContext();
        bucketContext.setCurrentBucket("test-bucket");

        // when
        boolean result = bucketContext.hasBucketSelected();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Given no bucket selected, when hasBucketSelected is called, then it should return false")
    void givenNoBucketSelected_whenHasBucketSelected_thenShouldReturnFalse() {
        // given
        BucketContext bucketContext = new BucketContext();

        // when
        boolean result = bucketContext.hasBucketSelected();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Given an empty bucket, when hasBucketSelected is called, then it should return false")
    void givenEmptyBucket_whenHasBucketSelected_thenShouldReturnFalse() {
        // given
        BucketContext bucketContext = new BucketContext();
        bucketContext.setCurrentBucket("");

        // when
        boolean result = bucketContext.hasBucketSelected();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Given a selected bucket, when clearCurrentBucket is called, then the bucket should be cleared")
    void givenSelectedBucket_whenClearCurrentBucket_thenBucketShouldBeCleared() {
        // given
        BucketContext bucketContext = new BucketContext();
        bucketContext.setCurrentBucket("test-bucket");

        // when
        bucketContext.clearCurrentBucket();

        // then
        assertThat(bucketContext.getCurrentBucket()).isNull();
        assertThat(bucketContext.hasBucketSelected()).isFalse();
    }
} 