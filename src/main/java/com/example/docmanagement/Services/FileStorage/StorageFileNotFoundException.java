package com.example.docmanagement.Services.FileStorage;

/**
 * Sprint 5: Exception for file not found scenarios
 */
public class StorageFileNotFoundException extends StorageException {

    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
