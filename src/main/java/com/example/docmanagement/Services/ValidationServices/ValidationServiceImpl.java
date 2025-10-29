package com.example.docmanagement.Services.ValidationServices;

import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ValidationServiceImpl implements ValidationService {

    private final UserRepository userRepository;
    private final SoftwareReleaseRepository releaseRepository;
    private final DocumentTypeRepository documentTypeRepository;


    public ValidationServiceImpl(UserRepository userRepository,
                                 SoftwareReleaseRepository releaseRepository,
                                 DocumentTypeRepository documentTypeRepository) {
        this.userRepository = userRepository;
        this.releaseRepository = releaseRepository;
        this.documentTypeRepository = documentTypeRepository;
    }

    @Override
    public void validateDocumentUploadPrerequisites(int uploaderId, int releaseId, int typeId) {
        if (!userRepository.existsById(uploaderId)) {
            throw new EntityNotFoundException("User not found with ID: " + uploaderId);
        }
        if (!releaseRepository.existsById(releaseId)) {
            throw new EntityNotFoundException("SoftwareRelease not found with ID: " + releaseId);
        }
        if (!documentTypeRepository.existsById(typeId)) {
            throw new EntityNotFoundException("DocumentType not found with ID: " + typeId);
        }
        // Aici pot fi adăugate și alte validări (ex: permisiuni utilizator)
    }
}
