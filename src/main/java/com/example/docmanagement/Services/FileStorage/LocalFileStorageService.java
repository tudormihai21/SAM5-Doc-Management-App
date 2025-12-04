package com.example.docmanagement.Services.FileStorage;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Sprint 5: Local File Storage Implementation
 *
 * Stores files on the local filesystem.
 * Designed to be easily replaceable with Azure Blob Storage implementation.
 *
 * Configuration via application.properties:
 * - file.upload-dir: Base directory for uploads (default: ./uploads)
 * - file.max-size: Maximum file size (configured in Spring properties)
 *
 * Note: Bean is created in FileStorageConfig, not via @Service annotation.
 * This allows for easier switching between storage implementations.
 */
public class LocalFileStorageService implements FileStorageService {

    private final Path rootLocation;

    /**
     * Constructor with configurable upload directory.
     * The uploadDir parameter is passed from FileStorageConfig.
     *
     * @param uploadDir Directory path for file storage
     */
    public LocalFileStorageService(String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println("File storage initialized at: " + rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(MultipartFile file, int documentId) {
        // Generate unique filename: documentId_timestamp_originalName
        String originalFilename = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename()));

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String extension = getFileExtension(originalFilename);
        String uniqueFilename = String.format("doc_%d_%s_%s%s",
                documentId,
                timestamp,
                UUID.randomUUID().toString().substring(0, 8),
                extension);

        return store(file, uniqueFilename);
    }

    @Override
    public String store(MultipartFile file, String filename) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file: " + filename);
            }

            // Security check: prevent path traversal attacks
            String cleanFilename = StringUtils.cleanPath(filename);
            if (cleanFilename.contains("..")) {
                throw new StorageException(
                        "Cannot store file with relative path outside current directory: " + filename);
            }

            // Create subdirectory based on year/month for better organization
            String yearMonth = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy/MM"));
            Path targetDir = rootLocation.resolve(yearMonth);
            Files.createDirectories(targetDir);

            Path destinationFile = targetDir.resolve(cleanFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return relative path from root location
            String storedPath = yearMonth + "/" + cleanFilename;
            System.out.println("File stored: " + storedPath);

            return storedPath;

        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(rootLocation, 10)
                    .filter(path -> !Files.isDirectory(path))
                    .map(rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename).normalize();
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public boolean delete(String filename) {
        try {
            Path file = load(filename);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
        init(); // Recreate the directory
    }

    @Override
    public boolean exists(String filename) {
        Path file = load(filename);
        return Files.exists(file);
    }

    @Override
    public String getStoragePath(String filename) {
        return load(filename).toString();
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }
}