package com.example.docmanagement;

import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Services.ValidationServices.ValidationService;
import com.example.docmanagement.Services.ValidationServices.ValidationServiceImpl;
import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentType;
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Repositories.UserRepository;
import com.example.docmanagement.Services.WorkFlowSer.DocumentWorkflowServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DocumentWorkflowServiceImplTest {

    @Mock
    private ValidationService validationService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SoftwareReleaseRepository releaseRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @InjectMocks
    private DocumentWorkflowServiceImpl documentWorkflowService;

    private User mockUser;
    private SoftwareRelease mockRelease;
    private DocumentType mockDocType;
    private Document mockDocument;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1);

        mockRelease = new SoftwareRelease();
        mockRelease.setReleaseId(1);

        mockDocType = new DocumentType();
        mockDocType.setTypeId(1);

        mockDocument = new Document();
        mockDocument.setDocumentId(100);
        mockDocument.setTitle("Test Doc");
        mockDocument.setUploader(mockUser);
        mockDocument.setSoftwareRelease(mockRelease);
        mockDocument.setDocumentType(mockDocType);
        mockDocument.setUploadTimestamp(LocalDateTime.now());
        mockDocument.setStatus(DocumentStatus.DRAFT);
    }

    @Test
    void testUploadNewDocument_Success() {
        DocumentStatus newStatus = DocumentStatus.DRAFT;
        doNothing().when(validationService).validateDocumentUploadPrerequisites(1, 1, 1);

        when(userRepository.getReferenceById(1)).thenReturn(mockUser);
        when(releaseRepository.getReferenceById(1)).thenReturn(mockRelease);
        when(documentTypeRepository.getReferenceById(1)).thenReturn(mockDocType);

        when(documentRepository.save(any(Document.class))).thenReturn(mockDocument);

        Document result = documentWorkflowService.uploadNewDocument(
                "Test Doc", "/path/test.pdf", "v1.0", 1, 1, 1,newStatus
        );

        assertNotNull(result);
        assertEquals(100, result.getDocumentId());
        assertEquals("Test Doc", result.getTitle());

        verify(validationService, times(1)).validateDocumentUploadPrerequisites(1, 1, 1);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void testUploadNewDocument_ValidationFails() {
        doThrow(new EntityNotFoundException("User not found"))
                .when(validationService).validateDocumentUploadPrerequisites(99, 1, 1);

        assertThrows(EntityNotFoundException.class, () -> {
            documentWorkflowService.uploadNewDocument(
                    "Test Doc", "/path/test.pdf", "v1.0", 99, 1, 1,DocumentStatus.DRAFT
            );
        });

        verify(documentRepository, never()).save(any(Document.class));
    }
}
