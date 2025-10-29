package com.example.docmanagement;

import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Repositories.UserRepository;
import com.example.docmanagement.Services.ValidationServices.ValidationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private SoftwareReleaseRepository releaseRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;


    @InjectMocks
    private ValidationServiceImpl validationService;

    @Test
    void testValidate_Success() {

        when(userRepository.existsById(1)).thenReturn(true);
        when(releaseRepository.existsById(1)).thenReturn(true);
        when(documentTypeRepository.existsById(1)).thenReturn(true);


        assertDoesNotThrow(() -> {
            validationService.validateDocumentUploadPrerequisites(1, 1, 1);
        });


        verify(userRepository, times(1)).existsById(1);
        verify(releaseRepository, times(1)).existsById(1);
        verify(documentTypeRepository, times(1)).existsById(1);
    }

    @Test
    void testValidate_UserNotFound() {

        when(userRepository.existsById(99)).thenReturn(false);



        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            validationService.validateDocumentUploadPrerequisites(99, 1, 1);
        });


        assertEquals("User not found with ID: 99", exception.getMessage());


        verify(releaseRepository, never()).existsById(anyInt());
        verify(documentTypeRepository, never()).existsById(anyInt());
    }

    @Test
    void testValidate_ReleaseNotFound() {

        when(userRepository.existsById(1)).thenReturn(true); // Utilizatorul există
        when(releaseRepository.existsById(99)).thenReturn(false); // Release-ul NU există


        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            validationService.validateDocumentUploadPrerequisites(1, 99, 1);
        });

        assertEquals("SoftwareRelease not found with ID: 99", exception.getMessage());


        verify(userRepository, times(1)).existsById(1);
        verify(documentTypeRepository, never()).existsById(anyInt());
    }

    @Test
    void testValidate_DocumentTypeNotFound() {

        when(userRepository.existsById(1)).thenReturn(true); // Utilizatorul există
        when(releaseRepository.existsById(1)).thenReturn(true); // Release-ul există
        when(documentTypeRepository.existsById(99)).thenReturn(false); // Tipul NU există


        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            validationService.validateDocumentUploadPrerequisites(1, 1, 99);
        });

        assertEquals("DocumentType not found with ID: 99", exception.getMessage());


        verify(userRepository, times(1)).existsById(1);
        verify(releaseRepository, times(1)).existsById(1);
    }
}