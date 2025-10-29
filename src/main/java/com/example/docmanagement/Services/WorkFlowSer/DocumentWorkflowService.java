package com.example.docmanagement.Services.WorkFlowSer;
import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;

public interface DocumentWorkflowService {

    Document uploadNewDocument(String title, String filePath, String version,
                               int uploaderId, int releaseId, int typeId,
                               DocumentStatus status);
}