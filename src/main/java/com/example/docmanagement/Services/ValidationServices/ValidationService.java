package com.example.docmanagement.Services.ValidationServices;

public interface ValidationService {

    void validateDocumentUploadPrerequisites(int uploaderId, int releaseId, int typeId);
}