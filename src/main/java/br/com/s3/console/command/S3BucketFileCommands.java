package br.com.s3.console.command;

import br.com.s3.console.context.BucketContext;
import br.com.s3.console.service.PrinterService;
import br.com.s3.console.service.S3BucketFileService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.nio.file.Path;
import java.util.List;

@ShellComponent
public class S3BucketFileCommands {

    private final S3BucketFileService s3BucketFileService;
    private final BucketContext bucketContext;
    private final PrinterService printerService;

    public S3BucketFileCommands(S3BucketFileService s3BucketFileService, BucketContext bucketContext, PrinterService printerService) {
        this.s3BucketFileService = s3BucketFileService;
        this.bucketContext = bucketContext;
        this.printerService = printerService;
    }

    @ShellMethod(key = "list-files", value = "Lists all files in the current S3 bucket")
    public String listFiles() {
        try {
            List<S3Object> objects = s3BucketFileService.listObjects();
            return printerService.formatObjectsTable(objects, bucketContext.getCurrentBucket());
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Error listing files: " + e.getMessage();
        }
    }

    @ShellMethod(key = "download-file", value = "Downloads a file from the current S3 bucket")
    public String downloadFile(
            @ShellOption(help = "File name in the bucket") String key,
            @ShellOption(help = "Destination directory", defaultValue = "./downloads") String destinationPath) {
        
        try {
            Path downloadedFilePath = s3BucketFileService.downloadObject(key, destinationPath);
            return "File downloaded successfully: " + downloadedFilePath;
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Error downloading file: " + e.getMessage();
        }
    }

    @ShellMethod(key = "delete-file", value = "Deletes a file from the current S3 bucket")
    public String deleteFile(@ShellOption(help = "File name in the bucket") String key) {
        try {
            s3BucketFileService.deleteObject(key);
            return "File '" + key + "' deleted successfully from bucket '" + bucketContext.getCurrentBucket() + "'";
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Error deleting file: " + e.getMessage();
        }
    }

    @ShellMethod(key = "upload-file", value = "Uploads a file to the current S3 bucket")
    public String uploadFile(
            @ShellOption(help = "Local file path") String filePath,
            @ShellOption(help = "File name in the bucket (optional)", defaultValue = "") String key) {
        
        try {
            // If key is not provided, use the file name
            if (key.isEmpty()) {
                key = Path.of(filePath).getFileName().toString();
            }
            
            s3BucketFileService.uploadObject(filePath, key);
            return "File '" + filePath + "' uploaded successfully to bucket '" + bucketContext.getCurrentBucket() + "' with key '" + key + "'";
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Error uploading file: " + e.getMessage();
        }
    }

    @ShellMethod(key = "wipe-files", value = "Removes all files from the current S3 bucket")
    public String clearBucket() {
        try {
            int deletedCount = s3BucketFileService.deleteAllObjects();
            
            if (deletedCount == 0) {
                return "Bucket '" + bucketContext.getCurrentBucket() + "' is already empty.";
            } else {
                return deletedCount + " file(s) deleted from bucket '" + bucketContext.getCurrentBucket() + "'.";
            }
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Error clearing bucket: " + e.getMessage();
        }
    }
} 