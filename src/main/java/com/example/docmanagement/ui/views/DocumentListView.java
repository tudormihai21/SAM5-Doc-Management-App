package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Repositories.DocumentRepository;

// Importă componentele UI de la Vaadin
import com.example.docmanagement.Services.Security.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route; // Importă adnotarea @Route
import jakarta.annotation.security.RolesAllowed;
import com.example.docmanagement.ui.views.MainLayout;

/**
 * Un ecran (view) care se va afișa la adresa URL "http://localhost:8080/"
 * Am schimbat în "" (gol) ca să fie pagina principală
 */
@Route(value = "", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_PROJECT_MANAGER", "ROLE_TEAM_MEMBER"})
public class DocumentListView extends VerticalLayout {

    private final DocumentRepository documentRepository;

    private Grid<Document> grid = new Grid<>(Document.class);

    public DocumentListView(DocumentRepository documentRepository, SecurityService securityService) {
        this.documentRepository = documentRepository;

        setSizeFull();
        configureGrid();


        add(grid);

        updateList();
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Vaadin încearcă să ghicească coloanele. Le scoatem pe cele pe care nu le vrem.
        grid.removeColumnByKey("documentId");
        grid.removeColumnByKey("filePath");
        grid.removeColumnByKey("featureLinks");
        grid.removeColumnByKey("softwareRelease");
        grid.removeColumnByKey("documentType");
        grid.removeColumnByKey("uploader");


        grid.setColumns("title", "documentVersion", "status", "uploadTimestamp");


        grid.addColumn(doc ->
                doc.getUploader() != null ?
                        doc.getUploader().getFirstName() + " " + doc.getUploader().getLastName() : "N/A"
        ).setHeader("Uploader");

        grid.addColumn(doc ->
                doc.getSoftwareRelease() != null ?
                        doc.getSoftwareRelease().getVersionNumber() : "N/A"
        ).setHeader("Release");

        grid.addColumn(doc ->
                doc.getDocumentType() != null ?
                        doc.getDocumentType().getTypeName() : "N/A"
        ).setHeader("Type");

        grid.addComponentColumn(document -> {
            if (document.getStatus() == DocumentStatus.PENDING_REVIEW) {
                boolean isManager = SecurityService.isCurrentUserProjectManager();

                Button approveButton = new Button("Aprobă");
                approveButton.addClickListener(e -> approveDocument(document));
                approveButton.setVisible(isManager);

                return approveButton;
            }
            return new VerticalLayout();
        }).setHeader("Acțiuni");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void updateList() {
        grid.setItems(documentRepository.findAllWithDetails());
    }

    private void approveDocument(Document document) {

        document.setStatus(DocumentStatus.APPROVED);
        documentRepository.save(document);
    }
}
