package com.example.docmanagement.Services.FileStorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Sprint 5: File Storage Service Interface
 * 
 * This interface abstracts file storage operations, making it easy to:
 * - Start with local file storage
 * - Later migrate to Azure Blob Storage or other cloud providers
 * 
 * Design Pattern: Strategy Pattern for storage backends
 */
public interface FileStorageService {

    /**
     * Initialize the storage location (create directories if needed)
     */
    void init();

    /**
     * Store a file and return the generated filename
     * 
     * @param file The uploaded file
     * @param documentId The associated document ID (for organizing files)
     * @return The stored filename (unique identifier)
     */
    String store(MultipartFile file, int documentId);

    /**
     * Store a file with a specific filename
     * 
     * @param file The uploaded file
     * @param filename The desired filename
     * @return The stored filename
     */
    String store(MultipartFile file, String filename);

    /**
     * Load all stored files
     * 
     * @return Stream of file paths
     */
    Stream<Path> loadAll();

    /**
     * Load a specific file path
     * 
     * @param filename The filename to load
     * @return Path to the file
     */
    Path load(String filename);

    /**
     * Load file as a Spring Resource (for download)
     * 
     * @param filename The filename to load
     * @return Resource for streaming
     */
    Resource loadAsResource(String filename);

    /**
     * Delete a specific file
     * 
     * @param filename The filename to delete
     * @return true if deleted successfully
     */
    boolean delete(String filename);

    /**
     * Delete all files (use with caution!)
     */
    void deleteAll();

    /**
     * Check if a file exists
     * 
     * @param filename The filename to check
     * @return true if file exists
     */
    boolean exists(String filename);

    /**
     * Get the full storage path for a file
     * 
     * @param filename The filename
     * @return Full path as string
     */
    String getStoragePath(String filename);
}
