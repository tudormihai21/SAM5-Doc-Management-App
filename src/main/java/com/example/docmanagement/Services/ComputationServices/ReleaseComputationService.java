package com.example.docmanagement.Services.ComputationServices;


public interface ReleaseComputationService {

    /**
     * Calculează numărul total de documente asociate unui ID de SoftwareRelease.
     *
     * @param releaseId ID-ul release-ului pentru care se numără documentele.
     * @return Numărul total de documente (long).
     */
    long countDocumentsForRelease(int releaseId);
}
