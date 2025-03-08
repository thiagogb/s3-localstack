package br.com.s3.console.context;

import org.springframework.stereotype.Component;

/**
 * Singleton class to store the selected bucket context
 */
@Component
public class BucketContext {
    
    private String currentBucket;
    
    /**
     * Sets the current bucket
     * @param bucketName Bucket name
     */
    public void setCurrentBucket(String bucketName) {
        this.currentBucket = bucketName;
    }
    
    /**
     * Gets the current bucket
     * @return Current bucket name or null if no bucket is selected
     */
    public String getCurrentBucket() {
        return currentBucket;
    }
    
    /**
     * Checks if a bucket is selected
     * @return true if a bucket is selected, false otherwise
     */
    public boolean hasBucketSelected() {
        return currentBucket != null && !currentBucket.isEmpty();
    }
    
    /**
     * Clears the selected bucket
     */
    public void clearCurrentBucket() {
        this.currentBucket = null;
    }
} 