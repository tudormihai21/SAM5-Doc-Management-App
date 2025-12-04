package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Services.FileStorage.FileStorageService;
import com.example.docmanagement.Services.Security.SecurityService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;

import java.io.InputStream;

/**
 * Sprint 5: Enhanced Document List View
 * 
 * Features:
 * - Document listing with details
 * - Download functionality for uploaded files
 * - Status badges
 * - Approve/Archive actions for managers
 * - Delete functionality
 */
@Route(value = "", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_PROJECT_MANAGER", "ROLE_TEAM_MEMBER"})
public class DocumentListView extends VerticalLayout {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final SecurityService securityService;

    private Grid<Document> grid = new Grid<>(Document.class, false);

    public DocumentListView(DocumentRepository documentRepository, 
                           FileStorageService fileStorageService,
                           SecurityService securityService) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.securityService = securityService;

        setSizeFull();
        configureGrid();
        add(grid);
        updateList();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Title column
        grid.addColumn(Document::getTitle)
                .setHeader("Title")
                .setSortable(true)
                .setFlexGrow(2);

        // Version column
        grid.addColumn(Document::getDocumentVersion)
                .setHeader("Version")
                .setSortable(true)
                .setWidth("100px");

        // Status column with badge styling
        grid.addComponentColumn(doc -> createStatusBadge(doc.getStatus()))
                .setHeader("Status")
                .setWidth("140px");

        // Upload timestamp
        grid.addColumn(doc -> doc.getUploadTimestamp() != null 
                ? doc.getUploadTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "N/A")
                .setHeader("Uploaded")
                .setSortable(true)
                .setWidth("150px");

        // Uploader column
        grid.addColumn(doc ->
                doc.getUploader() != null 
                        ? doc.getUploader().getFirstName() + " " + doc.getUploader().getLastName() 
                        : "N/A")
                .setHeader("Uploader")
                .setSortable(true);

        // Release column
        grid.addColumn(doc ->
                doc.getSoftwareRelease() != null 
                        ? doc.getSoftwareRelease().getVersionNumber() 
                        : "N/A")
                .setHeader("Release");

        // Type column
        grid.addColumn(doc ->
                doc.getDocumentType() != null 
                        ? doc.getDocumentType().getTypeName() 
                        : "N/A")
                .setHeader("Type");

        // Actions column
        grid.addComponentColumn(this::createActionsLayout)
                .setHeader("Actions")
                .setWidth("250px")
                .setFlexGrow(0);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private Span createStatusBadge(DocumentStatus status) {
        Span badge = new Span(getStatusDisplayName(status));
        badge.getElement().getThemeList().add("badge");
        
        switch (status) {
            case DRAFT -> badge.getElement().getThemeList().add("contrast");
            case PENDING_REVIEW -> badge.getElement().getThemeList().add("primary");
            case APPROVED -> badge.getElement().getThemeList().add("success");
            case ARCHIVED -> badge.getElement().getThemeList().add("error");
        }
        
        return badge;
    }

    private String getStatusDisplayName(DocumentStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case PENDING_REVIEW -> "Pending";
            case APPROVED -> "Approved";
            case ARCHIVED -> "Archived";
        };
    }

    private HorizontalLayout createActionsLayout(Document document) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        // Download button (if file exists)
        if (document.getFilePath() != null && !document.getFilePath().equals("pending")) {
            Button downloadButton = new Button(VaadinIcon.DOWNLOAD.create());
            downloadButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            downloadButton.getElement().setAttribute("title", "Download");
            
            // Create download link using StreamResource
            try {
                StreamResource resource = new StreamResource(
                        getDownloadFilename(document),
                        () -> {
                            try {
                                return fileStorageService.loadAsResource(document.getFilePath()).getInputStream();
                            } catch (Exception e) {
                                Notification.show("Error downloading file: " + e.getMessage(), 
                                        3000, Notification.Position.MIDDLE)
                                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                                return InputStream.nullInputStream();
                            }
                        }
                );
                
                Anchor downloadLink = new Anchor(resource, "");
                downloadLink.getElement().setAttribute("download", true);
                downloadLink.add(downloadButton);
                layout.add(downloadLink);
            } catch (Exception e) {
                // File doesn't exist, show disabled button
                downloadButton.setEnabled(false);
                layout.add(downloadButton);
            }
        }

        // Approve button (for managers, only if pending review)
        if (SecurityService.isCurrentUserProjectManager() && 
                document.getStatus() == DocumentStatus.PENDING_REVIEW) {
            Button approveButton = new Button(VaadinIcon.CHECK.create());
            approveButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
            approveButton.getElement().setAttribute("title", "Approve");
            approveButton.addClickListener(e -> approveDocument(document));
            layout.add(approveButton);
        }

        // Archive button (for managers and admins)
        if ((SecurityService.isCurrentUserProjectManager() || securityService.isCurrentUserAdmin()) &&
                document.getStatus() != DocumentStatus.ARCHIVED) {
            Button archiveButton = new Button(VaadinIcon.ARCHIVE.create());
            archiveButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            archiveButton.getElement().setAttribute("title", "Archive");
            archiveButton.addClickListener(e -> archiveDocument(document));
            layout.add(archiveButton);
        }

        // Delete button (for admins only)
        if (securityService.isCurrentUserAdmin()) {
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.getElement().setAttribute("title", "Delete");
            deleteButton.addClickListener(e -> confirmDelete(document));
            layout.add(deleteButton);
        }

        return layout;
    }

    private String getDownloadFilename(Document document) {
        String filePath = document.getFilePath();
        if (filePath != null && filePath.contains("/")) {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        }
        return document.getTitle();
    }

    private void approveDocument(Document document) {
        document.setStatus(DocumentStatus.APPROVED);
        documentRepository.save(document);
        updateList();
        
        Notification.show("Document '" + document.getTitle() + "' approved!", 
                3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void archiveDocument(Document document) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Archive Document");
        dialog.setText("Are you sure you want to archive '" + document.getTitle() + "'?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Archive");
        dialog.setConfirmButtonTheme("primary");
        
        dialog.addConfirmListener(e -> {
            document.setStatus(DocumentStatus.ARCHIVED);
            documentRepository.save(document);
            updateList();
            
            Notification.show("Document archived", 3000, Notification.Position.TOP_CENTER);
        });
        
        dialog.open();
    }

    private void confirmDelete(Document document) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Document");
        dialog.setText("Are you sure you want to permanently delete '" + document.getTitle() + "'? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> {
            try {
                // Delete physical file
                if (document.getFilePath() != null && !document.getFilePath().equals("pending")) {
                    fileStorageService.delete(document.getFilePath());
                }
                
                // Delete document record
                documentRepository.delete(document);
                updateList();
                
                Notification.show("Document deleted", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error deleting document: " + ex.getMessage(), 
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        dialog.open();
    }

    private void updateList() {
        grid.setItems(documentRepository.findAllWithDetails());
    }
}
