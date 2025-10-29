package com.example.docmanagement.Services.ComputationServices;

import com.example.docmanagement.Repositories.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class ReleaseComputationServiceImpl implements ReleaseComputationService {

    private final DocumentRepository documentRepository;


    public ReleaseComputationServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public long countDocumentsForRelease(int releaseId) {

        return documentRepository.countBySoftwareRelease_ReleaseId(releaseId);
    }
}