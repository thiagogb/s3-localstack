# S3 Console - Spring Shell Application for AWS S3

[![Java CI with Maven](https://github.com/yourusername/s3-console/actions/workflows/maven-test.yml/badge.svg)](https://github.com/yourusername/s3-console/actions/workflows/maven-test.yml)
[![Coverage](.github/badges/jacoco.svg)](.github/badges/jacoco.svg)
[![Branches](.github/badges/branches.svg)](.github/badges/branches.svg)

This Spring Shell application allows you to interact with AWS S3 buckets to perform operations such as listing, creating, and deleting buckets, as well as managing files within them. It was developed to work with LocalStack for local testing.

## Project Structure

The project is organized as follows:

- **Context**
  - `BucketContext`: Stores the currently selected bucket.

- **Services**
  - `S3BucketService`: Manages bucket-related operations (list, create, delete).
  - `S3BucketFileService`: Manages file operations within buckets (list, upload, download, delete).
  - `PrinterService`: Handles console output formatting.

- **Commands**
  - `S3BucketCommands`: Implements commands for managing buckets.
  - `S3BucketFileCommands`: Implements commands for managing files within buckets.

## Prerequisites

- Java 21
- Maven
- LocalStack (for local testing)

## LocalStack Setup

1. Install LocalStack:
   ```bash
   pip install localstack
   ```

2. Start LocalStack:
   ```bash
   localstack start
   ```

3. LocalStack will be available at `http://localhost:4566`

## Application Configuration

The application configuration is in the `src/main/resources/application.properties` file:

```properties
aws.s3.endpoint=http://localhost:4566
aws.region=us-east-1
aws.accessKey=test
aws.secretKey=test
```

## Building and Running

1. Build the application:
   ```bash
   mvn clean package
   ```

2. Run the application:
   ```bash
   java -jar target/console-0.0.1-SNAPSHOT.jar
   ```

## Available Commands

After starting the application, you'll have access to the following commands:

### Bucket Management

#### List Buckets

Lists all available buckets:
```
list-buckets
```

#### Create Bucket

Creates a new bucket:
```
create-bucket --name bucket-name
```

#### Delete Bucket

Deletes a bucket (must be empty):
```
delete-bucket --name bucket-name
```

#### Select Bucket

Selects a bucket for operations:
```
use-bucket --name bucket-name
```

#### Show Current Bucket

Shows which bucket is currently selected:
```
current-bucket
```

### File Management

**Note:** The commands below only work after selecting a bucket with the `use-bucket` command.

#### List Files

Lists all files in the current S3 bucket:
```
list-files
```

#### Download File

Downloads a file from the current S3 bucket:
```
download-file --key file-name [--destination-path ./path/to/destination]
```

#### Upload File

Uploads a file to the current S3 bucket:
```
upload-file --file-path ./path/to/file.txt [--key custom-name]
```

#### Delete File

Deletes a file from the current S3 bucket:
```
delete-file --key file-name
```

#### Clear Bucket

Removes all files from the current S3 bucket:
```
wipe-files
```

### Help

For help on available commands:
```
help
```

## Notes

- Downloaded files are saved by default in the `./downloads` directory.
- To exit the application, type `exit` or press `Ctrl+C`. 