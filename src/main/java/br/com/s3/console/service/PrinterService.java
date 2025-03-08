package br.com.s3.console.service;

import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service responsible for formatting and printing console output
 */
@Service
public class PrinterService {

    /**
     * Formats a list of buckets as a table for console output
     * 
     * @param buckets List of buckets to display
     * @return Formatted string with the table representation
     */
    public String formatBucketsTable(List<Bucket> buckets) {
        if (buckets.isEmpty()) {
            return "No buckets available.";
        }
        
        // Create table header
        String[][] data = new String[buckets.size() + 1][2];
        data[0] = new String[]{"Name", "Creation Date"};
        
        for (int i = 0; i < buckets.size(); i++) {
            Bucket bucket = buckets.get(i);
            data[i + 1] = new String[]{
                    bucket.name(),
                    bucket.creationDate() != null ? bucket.creationDate().toString() : "N/A"
            };
        }
        
        // Build and return formatted table
        return buildTable(data, "Available buckets:");
    }
    
    /**
     * Formats a list of S3 objects as a table for console output
     * 
     * @param objects List of S3 objects to display
     * @param bucketName Name of the bucket containing the objects
     * @return Formatted string with the table representation
     */
    public String formatObjectsTable(List<S3Object> objects, String bucketName) {
        if (objects.isEmpty()) {
            return "Bucket '" + bucketName + "' is empty.";
        }
        
        // Create table header
        String[][] data = new String[objects.size() + 1][4];
        data[0] = new String[]{"Name", "Size (bytes)", "Last Modified", "Storage Class"};
        
        // Fill table data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        
        for (int i = 0; i < objects.size(); i++) {
            S3Object obj = objects.get(i);
            data[i + 1] = new String[]{
                    obj.key(),
                    String.valueOf(obj.size()),
                    formatter.format(obj.lastModified()),
                    obj.storageClassAsString()
            };
        }
        
        // Build and return formatted table
        return buildTable(data, "Files in bucket '" + bucketName + "':");
    }
    
    /**
     * Builds a formatted table from data
     * 
     * @param data Table data including headers
     * @param title Title to display above the table
     * @return Formatted string with the table representation
     */
    private String buildTable(String[][] data, String title) {
        ArrayTableModel tableModel = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(tableModel);
        tableBuilder.addFullBorder(BorderStyle.fancy_light);
        
        return title + "\n" + tableBuilder.build().render(80);
    }
} 