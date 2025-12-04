package com.example.docmanagement.Config;

import com.example.docmanagement.Services.FileStorage.FileStorageService;
import com.example.docmanagement.Services.FileStorage.LocalFileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Sprint 5: File Storage Configuration
 * 
 * This configuration class manages the file storage service beans.
 * Currently configured for local storage, but designed to easily
 * switch to Azure Blob Storage when ready.
 * 
 * To switch storage backends:
 * 1. Implement AzureFileStorageService
 * 2. Update the @Primary annotation to the Azure implementation
 * 3. Or use profiles to switch between implementations
 */
@Configuration
public class FileStorageConfig {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * Primary file storage service bean.
     * Currently returns LocalFileStorageService.
     * 
     * To switch to Azure:
     * - Remove @Primary from this method
     * - Add @Primary to azureFileStorageService()
     */
    @Bean
    @Primary
    public FileStorageService localFileStorageService() {
        return new LocalFileStorageService(uploadDir);
    }

    /*
     * Future: Azure Blob Storage Service
     * Uncomment and implement when ready to migrate to Azure
     * 
     * @Bean
     * public FileStorageService azureFileStorageService(
     *         @Value("${azure.storage.connection-string}") String connectionString,
     *         @Value("${azure.storage.container-name}") String containerName) {
     *     return new AzureFileStorageService(connectionString, containerName);
     * }
     */
}
