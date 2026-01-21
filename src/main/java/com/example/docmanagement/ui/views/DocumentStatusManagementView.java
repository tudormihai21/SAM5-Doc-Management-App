package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.SoftwareProductRepository;
import com.example.docmanagement.Services.DocumentAccess.DocumentAccessService;
import com.example.docmanagement.Services.Security.SecurityService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * Sprint 5: Document Status Management View
 * 
 * Allows Project Manager and Admin to:
 * - View all documents with their statuses
 * - Change document status (Approve, Reject, Archive, etc.)
 * - Filter documents by status
 * - Bulk status updates
 * 
 * Note: This view is only for ADMIN and PROJECT_MANAGER, 
 * so no team filtering is needed - they have full access.
 */
@Route(value = "documents/status", layout = MainLayout.class)
@PageTitle("Document Status Management | DocManagement")
@RolesAllowed({"ROLE_ADMIN", "ROLE_PROJECT_MANAGER"})
public class DocumentStatusManagementView extends VerticalLayout {

    private final DocumentRepository documentRepository;
    private final SoftwareProductRepository productRepository;
    private final SecurityService securityService;
    private final DocumentAccessService documentAccessService;

    // Filter components
    private ComboBox<DocumentStatus> statusFilter = new ComboBox<>("Filter by Status");
    private ComboBox<SoftwareProduct> productFilter = new ComboBox<>("Filter by Product");
    private Button clearFiltersButton = new Button("Clear Filters", VaadinIcon.CLOSE.create());

    // Grid
    private Grid<Document> documentGrid = new Grid<>(Document.class, false);

    // Bulk actions
    private Button approveSelectedButton = new Button("Approve Selected", VaadinIcon.CHECK.create());
    private Button archiveSelectedButton = new Button("Archive Selected", VaadinIcon.ARCHIVE.create());

    public DocumentStatusManagementView(
            DocumentRepository documentRepository,
            SoftwareProductRepository productRepository,
            SecurityService securityService,
            DocumentAccessService documentAccessService) {
        
        this.documentRepository = documentRepository;
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.documentAccessService = documentAccessService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Document Status Management"));
        add(createFilterSection());
        add(createBulkActionsSection());
        add(createDocumentGrid());

        loadDocuments();
    }

    private HorizontalLayout createFilterSection() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(FlexComponent.Alignment.END);

        // Status filter
        statusFilter.setItems(DocumentStatus.values());
        statusFilter.setItemLabelGenerator(this::getStatusDisplayName);
        statusFilter.setPlaceholder("All statuses");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());

        // Product filter - uses accessible products (all for Admin/Manager)
        List<SoftwareProduct> accessibleProducts = documentAccessService.getAccessibleProducts();
        productFilter.setItems(accessibleProducts);
        productFilter.setItemLabelGenerator(SoftwareProduct::getProductName);
        productFilter.setPlaceholder("All products");
        productFilter.setClearButtonVisible(true);
        productFilter.addValueChangeListener(e -> applyFilters());

        // Clear filters button
        clearFiltersButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearFiltersButton.addClickListener(e -> {
            statusFilter.clear();
            productFilter.clear();
            loadDocuments();
        });

        filters.add(statusFilter, productFilter, clearFiltersButton);
        return filters;
    }

    private HorizontalLayout createBulkActionsSection() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();

        approveSelectedButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        approveSelectedButton.setEnabled(false);
        approveSelectedButton.addClickListener(e -> bulkUpdateStatus(DocumentStatus.APPROVED));

        archiveSelectedButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        archiveSelectedButton.setEnabled(false);
        archiveSelectedButton.addClickListener(e -> bulkUpdateStatus(DocumentStatus.ARCHIVED));

        Span hint = new Span("Select documents in the grid to enable bulk actions");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)");
        hint.getStyle().set("font-size", "var(--lumo-font-size-s)");

        actions.add(approveSelectedButton, archiveSelectedButton, hint);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        return actions;
    }

    private VerticalLayout createDocumentGrid() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSizeFull();

        // Configure grid
        documentGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        documentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        documentGrid.setSizeFull();

        // Columns
        documentGrid.addColumn(Document::getDocumentId)
                .setHeader("ID")
                .setWidth("70px")
                .setSortable(true);

        documentGrid.addColumn(Document::getTitle)
                .setHeader("Title")
                .setFlexGrow(2)
                .setSortable(true);

        documentGrid.addColumn(Document::getDocumentVersion)
                .setHeader("Version")
                .setWidth("100px");

        documentGrid.addComponentColumn(this::createStatusBadge)
                .setHeader("Current Status")
                .setWidth("140px");

        // Product column
        documentGrid.addColumn(doc -> {
            if (doc.getSoftwareRelease() != null && doc.getSoftwareRelease().getProduct() != null) {
                return doc.getSoftwareRelease().getProduct().getProductName();
            }
            return "N/A";
        }).setHeader("Product").setSortable(true);

        documentGrid.addColumn(doc -> 
                doc.getUploader() != null 
                        ? doc.getUploader().getFirstName() + " " + doc.getUploader().getLastName() 
                        : "N/A")
                .setHeader("Uploader")
                .setSortable(true);

        documentGrid.addColumn(doc -> 
                doc.getSoftwareRelease() != null 
                        ? doc.getSoftwareRelease().getVersionNumber() 
                        : "N/A")
                .setHeader("Release");

        documentGrid.addColumn(doc -> 
                doc.getUploadTimestamp() != null 
                        ? doc.getUploadTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : "N/A")
                .setHeader("Uploaded")
                .setSortable(true);

        documentGrid.addComponentColumn(this::createActions)
                .setHeader("Actions")
                .setWidth("200px")
                .setFlexGrow(0);

        // Selection listener for bulk actions
        documentGrid.asMultiSelect().addValueChangeListener(event -> {
            boolean hasSelection = !event.getValue().isEmpty();
            approveSelectedButton.setEnabled(hasSelection);
            archiveSelectedButton.setEnabled(hasSelection);
        });

        section.add(documentGrid);
        return section;
    }

    private Span createStatusBadge(Document document) {
        DocumentStatus status = document.getStatus();
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

    private HorizontalLayout createActions(Document document) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setPadding(false);

        // Change Status button
        Button changeStatusBtn = new Button(VaadinIcon.EDIT.create());
        changeStatusBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        changeStatusBtn.getElement().setAttribute("title", "Change Status");
        changeStatusBtn.addClickListener(e -> openStatusChangeDialog(document));

        // Quick Approve button (only for pending documents)
        if (document.getStatus() == DocumentStatus.PENDING_REVIEW) {
            Button approveBtn = new Button(VaadinIcon.CHECK.create());
            approveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
            approveBtn.getElement().setAttribute("title", "Approve");
            approveBtn.addClickListener(e -> quickApprove(document));
            actions.add(approveBtn);
        }

        // Quick Archive button (not for already archived)
        if (document.getStatus() != DocumentStatus.ARCHIVED) {
            Button archiveBtn = new Button(VaadinIcon.ARCHIVE.create());
            archiveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            archiveBtn.getElement().setAttribute("title", "Archive");
            archiveBtn.addClickListener(e -> confirmArchive(document));
            actions.add(archiveBtn);
        }

        // Send back to Draft (for rejected/revision needed)
        if (document.getStatus() == DocumentStatus.APPROVED || document.getStatus() == DocumentStatus.PENDING_REVIEW) {
            Button rejectBtn = new Button(VaadinIcon.ARROW_BACKWARD.create());
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            rejectBtn.getElement().setAttribute("title", "Send back to Draft");
            rejectBtn.addClickListener(e -> sendBackToDraft(document));
            actions.add(rejectBtn);
        }

        actions.add(changeStatusBtn);
        return actions;
    }

    private void openStatusChangeDialog(Document document) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Change Document Status");
        dialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // Document info
        Span docInfo = new Span("Document: " + document.getTitle());
        docInfo.getStyle().set("font-weight", "bold");

        Span currentStatus = new Span("Current Status: " + getStatusDisplayName(document.getStatus()));

        // Status selection
        ComboBox<DocumentStatus> newStatusCombo = new ComboBox<>("New Status");
        newStatusCombo.setItems(DocumentStatus.values());
        newStatusCombo.setItemLabelGenerator(this::getStatusDisplayName);
        newStatusCombo.setValue(document.getStatus());
        newStatusCombo.setWidthFull();

        // Comment field
        TextArea commentField = new TextArea("Comment (optional)");
        commentField.setPlaceholder("Add a note about this status change...");
        commentField.setWidthFull();
        commentField.setMaxLength(500);

        content.add(docInfo, currentStatus, newStatusCombo, commentField);
        dialog.add(content);

        // Buttons
        Button saveBtn = new Button("Save", e -> {
            if (newStatusCombo.getValue() != null) {
                updateDocumentStatus(document, newStatusCombo.getValue(), commentField.getValue());
                dialog.close();
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void updateDocumentStatus(Document document, DocumentStatus newStatus, String comment) {
        DocumentStatus oldStatus = document.getStatus();
        document.setStatus(newStatus);
        documentRepository.save(document);

        String message = "Document '" + document.getTitle() + "' status changed from " 
                + getStatusDisplayName(oldStatus) + " to " + getStatusDisplayName(newStatus);
        
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        loadDocuments();
    }

    private void quickApprove(Document document) {
        document.setStatus(DocumentStatus.APPROVED);
        documentRepository.save(document);
        
        Notification.show("Document '" + document.getTitle() + "' approved!", 
                2000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        
        loadDocuments();
    }

    private void confirmArchive(Document document) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Archive Document");
        dialog.setText("Are you sure you want to archive '" + document.getTitle() + "'?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Archive");
        dialog.setConfirmButtonTheme("primary");
        
        dialog.addConfirmListener(e -> {
            document.setStatus(DocumentStatus.ARCHIVED);
            documentRepository.save(document);
            
            Notification.show("Document archived", 2000, Notification.Position.TOP_CENTER);
            loadDocuments();
        });
        
        dialog.open();
    }

    private void sendBackToDraft(Document document) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Send Back to Draft");
        dialog.setText("Send '" + document.getTitle() + "' back to Draft status? This typically means revisions are needed.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Send to Draft");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> {
            document.setStatus(DocumentStatus.DRAFT);
            documentRepository.save(document);
            
            Notification.show("Document sent back to Draft", 2000, Notification.Position.TOP_CENTER);
            loadDocuments();
        });
        
        dialog.open();
    }

    private void bulkUpdateStatus(DocumentStatus newStatus) {
        var selectedDocuments = documentGrid.asMultiSelect().getValue();
        
        if (selectedDocuments.isEmpty()) {
            Notification.show("No documents selected", 2000, Notification.Position.MIDDLE);
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Bulk Status Update");
        dialog.setText("Update " + selectedDocuments.size() + " document(s) to " + getStatusDisplayName(newStatus) + "?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Update All");
        dialog.setConfirmButtonTheme("primary");
        
        dialog.addConfirmListener(e -> {
            int count = 0;
            for (Document doc : selectedDocuments) {
                doc.setStatus(newStatus);
                documentRepository.save(doc);
                count++;
            }
            
            Notification.show(count + " document(s) updated to " + getStatusDisplayName(newStatus), 
                    3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            documentGrid.asMultiSelect().clear();
            loadDocuments();
        });
        
        dialog.open();
    }

    private void applyFilters() {
        // Get all accessible documents (all for Admin/Manager)
        List<Document> documents = documentAccessService.getAccessibleDocuments();
        
        // Apply status filter
        if (statusFilter.getValue() != null) {
            documents = documents.stream()
                    .filter(d -> d.getStatus() == statusFilter.getValue())
                    .toList();
        }
        
        // Apply product filter
        if (productFilter.getValue() != null) {
            int productId = productFilter.getValue().getProductId();
            documents = documents.stream()
                    .filter(d -> d.getSoftwareRelease() != null 
                            && d.getSoftwareRelease().getProduct() != null
                            && d.getSoftwareRelease().getProduct().getProductId() == productId)
                    .toList();
        }
        
        documentGrid.setItems(documents);
    }

    private void loadDocuments() {
        applyFilters();
    }

    private String getStatusDisplayName(DocumentStatus status) {
        if (status == null) return "Unknown";
        return switch (status) {
            case DRAFT -> "Draft";
            case PENDING_REVIEW -> "Pending Review";
            case APPROVED -> "Approved";
            case ARCHIVED -> "Archived";
        };
    }
}
