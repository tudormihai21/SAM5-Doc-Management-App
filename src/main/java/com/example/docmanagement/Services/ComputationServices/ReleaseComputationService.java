package com.example.docmanagement.Services.ComputationServices;


public interface ReleaseComputationService {

    /**
     * Calculam nr total de documente asociate unui ID de SoftwareRelease.
     */
    long countDocumentsForRelease(int releaseId);
}
