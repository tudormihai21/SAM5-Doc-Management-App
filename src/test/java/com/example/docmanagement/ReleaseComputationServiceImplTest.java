package com.example.docmanagement;

import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Services.ComputationServices.ReleaseComputationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseComputationServiceImplTest {


    @Mock
    private DocumentRepository documentRepository;


    @InjectMocks
    private ReleaseComputationServiceImpl computationService;

    @Test
    void testCountDocumentsForRelease_Success() {

        int testReleaseId = 5;
        long expectedCount = 3L;



        when(documentRepository.countBySoftwareRelease_ReleaseId(testReleaseId))
                .thenReturn(expectedCount);


        long actualCount = computationService.countDocumentsForRelease(testReleaseId);


        assertEquals(expectedCount, actualCount);


        verify(documentRepository, times(1)).countBySoftwareRelease_ReleaseId(testReleaseId);
    }

    @Test
    void testCountDocumentsForRelease_NoDocuments() {

        int testReleaseId = 10;
        long expectedCount = 0L;


        when(documentRepository.countBySoftwareRelease_ReleaseId(testReleaseId))
                .thenReturn(expectedCount);


        long actualCount = computationService.countDocumentsForRelease(testReleaseId);


        assertEquals(expectedCount, actualCount);
        verify(documentRepository, times(1)).countBySoftwareRelease_ReleaseId(testReleaseId);
    }
}
