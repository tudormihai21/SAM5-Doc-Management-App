package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.SoftwareProductRepository;
import com.example.docmanagement.ui.views.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Collections;

@Route(value = "projects", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_PROJECT_MANAGER", "ROLE_TEAM_MEMBER"})
public class ProjectDashboardView extends VerticalLayout {

    private final DocumentRepository documentRepository;
    private final SoftwareProductRepository productRepository;

    private ComboBox<SoftwareProduct> projectSelector = new ComboBox<>("Selectează Proiectul");
    private Grid<Document> documentGrid = new Grid<>(Document.class);

    public ProjectDashboardView(DocumentRepository documentRepository, SoftwareProductRepository productRepository) {
        this.documentRepository = documentRepository;
        this.productRepository = productRepository;

        setSizeFull();

        // Configurăm dropdown-ul
        projectSelector.setItems(productRepository.findAll());
        projectSelector.setItemLabelGenerator(SoftwareProduct::getProductName);
        projectSelector.setWidth("50%");

        // Configurăm tabelul
        configureGrid();

        // Adăugăm logica de filtrare
        projectSelector.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                // Dacă un proiect e selectat, încărcăm documentele lui
                updateGrid(event.getValue());
            } else {
                // Dacă nimic nu e selectat, golim tabelul
                documentGrid.setItems(Collections.emptyList());
            }
        });

        add(projectSelector, documentGrid);
    }

    private void configureGrid() {
        // Folosim aceeași configurare ca în DocumentListView
        documentGrid.setSizeFull();
        documentGrid.removeColumnByKey("documentId");
        documentGrid.removeColumnByKey("filePath");
        documentGrid.removeColumnByKey("featureLinks");
        documentGrid.removeColumnByKey("softwareRelease");
        documentGrid.removeColumnByKey("documentType");
        documentGrid.removeColumnByKey("uploader");

        documentGrid.setColumns("title", "documentVersion", "status", "uploadTimestamp");

        documentGrid.addColumn(doc ->
                doc.getUploader() != null ?
                        doc.getUploader().getFirstName() + " " + doc.getUploader().getLastName() : "N/A"
        ).setHeader("Uploader");

        documentGrid.addColumn(doc ->
                doc.getDocumentType() != null ?
                        doc.getDocumentType().getTypeName() : "N/A"
        ).setHeader("Type");

        documentGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void updateGrid(SoftwareProduct product) {
        // Apelăm metoda NOUĂ din repository, care evită eroarea Lazy
        documentGrid.setItems(documentRepository.findByProductIdWithDetails(product.getProductId()));
    }
}