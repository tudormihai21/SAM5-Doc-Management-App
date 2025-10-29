package com.example.docmanagement.Services.ComputationServices;

import com.example.docmanagement.Repositories.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class ReleaseComputationServiceImpl implements ReleaseComputationService {

    private final DocumentRepository documentRepository;

    // Injectăm singura lui dependență: Ajutorul care știe să numere
    public ReleaseComputationServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public long countDocumentsForRelease(int releaseId) {
        // Logica este simplă: delegăm numărătoarea către Repository.
        // Repository-ul o va face eficient la nivel de bază de date.
        return documentRepository.countBySoftwareRelease_ReleaseId(releaseId);
    }
}