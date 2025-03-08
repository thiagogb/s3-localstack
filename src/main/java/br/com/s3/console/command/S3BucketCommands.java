package br.com.s3.console.command;

import br.com.s3.console.context.BucketContext;
import br.com.s3.console.service.PrinterService;
import br.com.s3.console.service.S3BucketService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.util.List;

@ShellComponent
public class S3BucketCommands {

    private final S3BucketService s3BucketService;
    private final BucketContext bucketContext;
    private final PrinterService printerService;

    public S3BucketCommands(S3BucketService s3BucketService, BucketContext bucketContext, PrinterService printerService) {
        this.s3BucketService = s3BucketService;
        this.bucketContext = bucketContext;
        this.printerService = printerService;
    }

    @ShellMethod(key = "list-buckets", value = "Lists all available buckets")
    public String listBuckets() {
        List<Bucket> buckets = s3BucketService.listBuckets();
        return printerService.formatBucketsTable(buckets);
    }

    @ShellMethod(key = "create-bucket", value = "Creates a new bucket")
    public String createBucket(@ShellOption(help = "Bucket name") String name) {
        try {
            if (s3BucketService.bucketExists(name)) {
                return "Bucket '" + name + "' already exists.";
            }
            
            s3BucketService.createBucket(name);
            return "Bucket '" + name + "' created successfully.";
        } catch (Exception e) {
            return "Error creating bucket: " + e.getMessage();
        }
    }

    @ShellMethod(key = "delete-bucket", value = "Deletes a bucket")
    public String deleteBucket(@ShellOption(help = "Bucket name") String name) {
        try {
            if (!s3BucketService.bucketExists(name)) {
                return "Bucket '" + name + "' does not exist.";
            }
            
            s3BucketService.deleteBucket(name);
            return "Bucket '" + name + "' deleted successfully.";
        } catch (Exception e) {
            return "Error deleting bucket: " + e.getMessage();
        }
    }

    @ShellMethod(key = "use-bucket", value = "Selects a bucket for operations")
    public String useBucket(@ShellOption(help = "Bucket name") String name) {
        try {
            if (!s3BucketService.bucketExists(name)) {
                return "Bucket '" + name + "' does not exist. Use the 'create-bucket' command to create it.";
            }
            
            bucketContext.setCurrentBucket(name);
            return "Bucket '" + name + "' selected successfully.";
        } catch (Exception e) {
            return "Error selecting bucket: " + e.getMessage();
        }
    }

    @ShellMethod(key = "current-bucket", value = "Shows the currently selected bucket")
    public String currentBucket() {
        if (!bucketContext.hasBucketSelected()) {
            return "No bucket selected. Use the 'use-bucket' command to select a bucket.";
        }
        
        return "Current bucket: " + bucketContext.getCurrentBucket();
    }
} 