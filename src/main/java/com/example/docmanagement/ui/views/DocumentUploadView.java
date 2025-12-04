package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Domain.Document.DocumentType;
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Services.FileStorage.ByteArrayMultipartFile;
import com.example.docmanagement.Services.FileStorage.FileStorageService;
import com.example.docmanagement.Services.Security.SecurityService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Sprint 5: Document Upload View
 * 
 * Vaadin-based web interface for uploading documents.
 * Features:
 * - Drag & drop file upload
 * - Document metadata form
 * - Progress indication
 * - Validation and error handling
 */
@Route(value = "upload", layout = MainLayout.class)
@PageTitle("Upload Document | DocManagement")
@RolesAllowed({"ROLE_ADMIN", "ROLE_PROJECT_MANAGER", "ROLE_TEAM_MEMBER"})
public class DocumentUploadView extends VerticalLayout {

    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;
    private final SoftwareReleaseRepository releaseRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final SecurityService securityService;

    // Form fields
    private TextField titleField = new TextField("Document Title");
    private TextField versionField = new TextField("Version");
    private ComboBox<SoftwareRelease> releaseComboBox = new ComboBox<>("Software Release");
    private ComboBox<DocumentType> typeComboBox = new ComboBox<>("Document Type");
    private ComboBox<DocumentStatus> statusComboBox = new ComboBox<>("Status");
    
    // Upload component
    private MemoryBuffer buffer = new MemoryBuffer();
    private Upload upload = new Upload(buffer);
    
    // Status display
    private Span uploadStatus = new Span();
    private ProgressBar progressBar = new ProgressBar();
    private Button submitButton = new Button("Upload Document", VaadinIcon.UPLOAD.create());

    // Track uploaded file info
    private String uploadedFileName = null;
    private String uploadedContentType = null;
    private byte[] uploadedFileData = null;

    public DocumentUploadView(
            FileStorageService fileStorageService,
            DocumentRepository documentRepository,
            SoftwareReleaseRepository releaseRepository,
            DocumentTypeRepository documentTypeRepository,
            SecurityService securityService) {
        
        this.fileStorageService = fileStorageService;
        this.documentRepository = documentRepository;
        this.releaseRepository = releaseRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createUploadSection());
        add(createMetadataForm());
        add(createSubmitSection());
    }

    private H2 createHeader() {
        H2 header = new H2("Upload New Document");
        header.getStyle().set("margin-top", "0");
        return header;
    }

    private VerticalLayout createUploadSection() {
        VerticalLayout uploadSection = new VerticalLayout();
        uploadSection.setPadding(false);
        uploadSection.setSpacing(true);

        // Configure upload component
        upload.setAcceptedFileTypes(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain",
                "text/csv",
                "image/*"
        );
        upload.setMaxFiles(1);
        upload.setMaxFileSize(50 * 1024 * 1024); // 50MB max
        upload.setDropAllowed(true);

        // Upload button text
        Button uploadButton = new Button("Select File");
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(uploadButton);

        // Drop label
        Span dropLabel = new Span("Drop file here or click to upload");
        upload.setDropLabel(dropLabel);

        // Event listeners
        upload.addSucceededListener(event -> {
            try {
                uploadedFileName = event.getFileName();
                uploadedContentType = event.getMIMEType();
                uploadedFileData = buffer.getInputStream().readAllBytes();
                
                uploadStatus.setText("File ready: " + uploadedFileName + 
                        " (" + formatFileSize(uploadedFileData.length) + ")");
                uploadStatus.getElement().getThemeList().add("badge success");
                
                // Auto-fill title if empty
                if (titleField.isEmpty()) {
                    String nameWithoutExtension = uploadedFileName;
                    int lastDot = uploadedFileName.lastIndexOf('.');
                    if (lastDot > 0) {
                        nameWithoutExtension = uploadedFileName.substring(0, lastDot);
                    }
                    titleField.setValue(nameWithoutExtension);
                }
                
                submitButton.setEnabled(true);
            } catch (Exception e) {
                uploadStatus.setText("Error reading file: " + e.getMessage());
                uploadStatus.getElement().getThemeList().add("badge error");
            }
        });

        upload.addFailedListener(event -> {
            uploadStatus.setText("Upload failed: " + event.getReason().getMessage());
            uploadStatus.getElement().getThemeList().add("badge error");
            submitButton.setEnabled(false);
        });

        upload.addFileRejectedListener(event -> {
            Notification.show("File rejected: " + event.getErrorMessage(), 
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // Progress bar (hidden by default)
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);

        uploadSection.add(
                new Paragraph("Supported formats: PDF, Word, Excel, Text, CSV, Images (max 50MB)"),
                upload,
                uploadStatus,
                progressBar
        );

        return uploadSection;
    }

    private FormLayout createMetadataForm() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Title field
        titleField.setRequired(true);
        titleField.setPlaceholder("Enter document title");
        titleField.setWidthFull();

        // Version field
        versionField.setRequired(true);
        versionField.setValue("1.0");
        versionField.setPlaceholder("e.g., 1.0, 2.1");

        // Release combo box
        releaseComboBox.setItems(releaseRepository.findAllWithProduct());
        releaseComboBox.setItemLabelGenerator(release -> 
                release.getVersionNumber() + " (" + 
                (release.getProduct() != null ? release.getProduct().getProductName() : "N/A") + ")");
        releaseComboBox.setRequired(true);
        releaseComboBox.setPlaceholder("Select release");

        // Type combo box
        typeComboBox.setItems(documentTypeRepository.findAll());
        typeComboBox.setItemLabelGenerator(DocumentType::getTypeName);
        typeComboBox.setRequired(true);
        typeComboBox.setPlaceholder("Select document type");

        // Status combo box
        statusComboBox.setItems(DocumentStatus.values());
        statusComboBox.setValue(DocumentStatus.DRAFT);
        statusComboBox.setItemLabelGenerator(status -> {
            return switch (status) {
                case DRAFT -> "Draft";
                case PENDING_REVIEW -> "Pending Review";
                case APPROVED -> "Approved";
                case ARCHIVED -> "Archived";
            };
        });

        form.add(titleField, versionField, releaseComboBox, typeComboBox, statusComboBox);
        form.setColspan(titleField, 2);

        return form;
    }

    private HorizontalLayout createSubmitSection() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setPadding(true);

        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        submitButton.setEnabled(false); // Disabled until file is uploaded
        submitButton.addClickListener(e -> handleSubmit());

        Button clearButton = new Button("Clear", VaadinIcon.TRASH.create());
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearButton.addClickListener(e -> clearForm());

        buttonLayout.add(clearButton, submitButton);
        return buttonLayout;
    }

    private void handleSubmit() {
        // Validate form
        if (!validateForm()) {
            return;
        }

        try {
            progressBar.setVisible(true);
            submitButton.setEnabled(false);

            // Get current user
            Optional<User> currentUser = SecurityService.getAuthenticatedUser();
            if (currentUser.isEmpty()) {
                showError("Error: Could not identify current user");
                return;
            }

            // Create document entity
            Document document = new Document();
            document.setTitle(titleField.getValue());
            document.setDocumentVersion(versionField.getValue());
            document.setUploadTimestamp(LocalDateTime.now());
            document.setUploader(currentUser.get());
            document.setSoftwareRelease(releaseComboBox.getValue());
            document.setDocumentType(typeComboBox.getValue());
            document.setStatus(statusComboBox.getValue());
            document.setFilePath("pending"); // Temporary

            // Save document to get ID
            Document savedDoc = documentRepository.save(document);

            // Store file using the service with our custom MultipartFile wrapper
            ByteArrayMultipartFile multipartFile = new ByteArrayMultipartFile(
                    uploadedFileData,
                    "file",
                    uploadedFileName,
                    uploadedContentType
            );
            
            String storedPath = fileStorageService.store(multipartFile, savedDoc.getDocumentId());

            // Update document with file path
            savedDoc.setFilePath(storedPath);
            documentRepository.save(savedDoc);

            // Success notification
            Notification notification = Notification.show(
                    "Document '" + savedDoc.getTitle() + "' uploaded successfully!",
                    4000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Clear form for next upload
            clearForm();

        } catch (Exception e) {
            showError("Upload failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            progressBar.setVisible(false);
            submitButton.setEnabled(true);
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (uploadedFileData == null || uploadedFileData.length == 0) {
            errors.append("Please select a file to upload.\n");
        }
        if (titleField.isEmpty()) {
            errors.append("Document title is required.\n");
        }
        if (versionField.isEmpty()) {
            errors.append("Version is required.\n");
        }
        if (releaseComboBox.isEmpty()) {
            errors.append("Please select a software release.\n");
        }
        if (typeComboBox.isEmpty()) {
            errors.append("Please select a document type.\n");
        }

        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void clearForm() {
        titleField.clear();
        versionField.setValue("1.0");
        releaseComboBox.clear();
        typeComboBox.clear();
        statusComboBox.setValue(DocumentStatus.DRAFT);
        uploadStatus.setText("");
        uploadStatus.getElement().getThemeList().clear();
        uploadedFileName = null;
        uploadedContentType = null;
        uploadedFileData = null;
        submitButton.setEnabled(false);
        
        // Clear the upload component
        upload.getElement().executeJs("this.files = []");
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
