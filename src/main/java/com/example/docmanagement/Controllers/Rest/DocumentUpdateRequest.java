package com.example.docmanagement.Controllers.Rest;

import com.example.docmanagement.Domain.Document.DocumentStatus;
/**
 DTO for updating documents
 **/
public record DocumentUpdateRequest(
        String title,
        String version,
        DocumentStatus status
) {}
