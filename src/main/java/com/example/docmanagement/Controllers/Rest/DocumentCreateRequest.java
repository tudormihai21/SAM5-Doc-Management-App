package com.example.docmanagement.Controllers.Rest;

import com.example.docmanagement.Domain.Document.DocumentStatus;
/**
 DTO for creating documents
 **/
public record DocumentCreateRequest(
        String title,
        String filePath,
        String version,
        int uploaderId,
        int releaseId,
        int typeId,
        DocumentStatus status
) {}
