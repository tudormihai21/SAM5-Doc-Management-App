package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.Document.DocumentType;
import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import com.example.docmanagement.Domain.Team.Team;
import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareProductRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Repositories.TeamRepository;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.List;

/**
 * Sprint 5: Project Creation View (Updated)
 *
 * Allows Admin and Project Manager to:
 * - Create new Software Products (Projects)
 * - Create new Software Releases for products
 * - Create new Document Types
 * - View existing products, releases, and document types
 */
@Route(value = "projects/create", layout = MainLayout.class)
@PageTitle("Create Project | DocManagement")
@RolesAllowed({"ROLE_ADMIN", "ROLE_PROJECT_MANAGER"})
public class ProjectCreationView extends VerticalLayout {

    private final SoftwareProductRepository productRepository;
    private final SoftwareReleaseRepository releaseRepository;
    private final TeamRepository teamRepository;
    private final DocumentTypeRepository documentTypeRepository;

    // Tabs for different sections
    private Tabs tabs;
    private Tab productsTab;
    private Tab documentTypesTab;

    // Content containers
    private VerticalLayout productsContent;
    private VerticalLayout documentTypesContent;

    // Product form fields
    private TextField productNameField = new TextField("Product Name");
    private ComboBox<Team> ownerTeamComboBox = new ComboBox<>("Owner Team");
    private Button saveProductButton = new Button("Create Product", VaadinIcon.PLUS.create());

    // Release form fields
    private ComboBox<SoftwareProduct> productComboBox = new ComboBox<>("Select Product");
    private TextField versionNumberField = new TextField("Version Number");
    private DatePicker releaseDatePicker = new DatePicker("Release Date");
    private Button saveReleaseButton = new Button("Create Release", VaadinIcon.PLUS.create());

    // Document Type form fields
    private TextField documentTypeNameField = new TextField("Document Type Name");
    private Button saveDocumentTypeButton = new Button("Create Document Type", VaadinIcon.PLUS.create());

    // Grids
    private Grid<SoftwareProduct> productGrid = new Grid<>(SoftwareProduct.class, false);
    private Grid<SoftwareRelease> releaseGrid = new Grid<>(SoftwareRelease.class, false);
    private Grid<DocumentType> documentTypeGrid = new Grid<>(DocumentType.class, false);

    // Binders
    private Binder<SoftwareProduct> productBinder = new Binder<>(SoftwareProduct.class);
    private Binder<SoftwareRelease> releaseBinder = new Binder<>(SoftwareRelease.class);

    public ProjectCreationView(
            SoftwareProductRepository productRepository,
            SoftwareReleaseRepository releaseRepository,
            TeamRepository teamRepository,
            DocumentTypeRepository documentTypeRepository) {

        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.teamRepository = teamRepository;
        this.documentTypeRepository = documentTypeRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Project & Configuration Management"));

        // Create tabs
        createTabs();

        // Create content sections
        createProductsContent();
        createDocumentTypesContent();

        // Add tabs and initial content
        add(tabs);
        add(productsContent);
        add(documentTypesContent);

        // Show products tab by default
        documentTypesContent.setVisible(false);

        // Load initial data
        refreshData();
    }

    private void createTabs() {
        productsTab = new Tab(VaadinIcon.PACKAGE.create(), new Span("Products & Releases"));
        documentTypesTab = new Tab(VaadinIcon.FILE_TEXT_O.create(), new Span("Document Types"));

        tabs = new Tabs(productsTab, documentTypesTab);
        tabs.addSelectedChangeListener(event -> {
            productsContent.setVisible(tabs.getSelectedTab() == productsTab);
            documentTypesContent.setVisible(tabs.getSelectedTab() == documentTypesTab);
        });
    }

    private void createProductsContent() {
        productsContent = new VerticalLayout();
        productsContent.setPadding(false);
        productsContent.setSpacing(true);
        productsContent.setSizeFull();

        // Product creation section
        productsContent.add(createProductSection());
        productsContent.add(new Hr());

        // Release creation section
        productsContent.add(createReleaseSection());
        productsContent.add(new Hr());

        // Existing products grid
        productsContent.add(createProductsGrid());
    }

    private void createDocumentTypesContent() {
        documentTypesContent = new VerticalLayout();
        documentTypesContent.setPadding(false);
        documentTypesContent.setSpacing(true);
        documentTypesContent.setSizeFull();

        // Document Type creation section
        documentTypesContent.add(createDocumentTypeSection());
        documentTypesContent.add(new Hr());

        // Existing document types grid
        documentTypesContent.add(createDocumentTypesGrid());
    }

    private VerticalLayout createProductSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        section.add(new H3("Create New Product"));

        // Configure fields
        productNameField.setRequired(true);
        productNameField.setPlaceholder("Enter product name");
        productNameField.setWidthFull();

        ownerTeamComboBox.setItems(teamRepository.findAll());
        ownerTeamComboBox.setItemLabelGenerator(Team::getTeamName);
        ownerTeamComboBox.setPlaceholder("Select owner team (optional)");
        ownerTeamComboBox.setWidthFull();

        // Bind fields
        productBinder.forField(productNameField)
                .asRequired("Product name is required")
                .bind(SoftwareProduct::getProductName, SoftwareProduct::setProductName);
        productBinder.forField(ownerTeamComboBox)
                .bind(SoftwareProduct::getOwnerTeam, SoftwareProduct::setOwnerTeam);

        // Form layout
        FormLayout form = new FormLayout();
        form.add(productNameField, ownerTeamComboBox);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Save button
        saveProductButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveProductButton.addClickListener(e -> saveProduct());

        section.add(form, saveProductButton);
        return section;
    }

    private VerticalLayout createReleaseSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        section.add(new H3("Create New Release"));

        // Configure fields
        productComboBox.setItems(productRepository.findAll());
        productComboBox.setItemLabelGenerator(SoftwareProduct::getProductName);
        productComboBox.setRequired(true);
        productComboBox.setPlaceholder("Select product");
        productComboBox.setWidthFull();

        versionNumberField.setRequired(true);
        versionNumberField.setPlaceholder("e.g., 1.0.0, 2.1.0-BETA");
        versionNumberField.setWidthFull();

        releaseDatePicker.setValue(LocalDate.now());
        releaseDatePicker.setWidthFull();

        // Bind fields
        releaseBinder.forField(productComboBox)
                .asRequired("Product is required")
                .bind(SoftwareRelease::getProduct, SoftwareRelease::setProduct);
        releaseBinder.forField(versionNumberField)
                .asRequired("Version number is required")
                .bind(SoftwareRelease::getVersionNumber, SoftwareRelease::setVersionNumber);
        releaseBinder.forField(releaseDatePicker)
                .bind(SoftwareRelease::getReleaseDate, SoftwareRelease::setReleaseDate);

        // Form layout
        FormLayout form = new FormLayout();
        form.add(productComboBox, versionNumberField, releaseDatePicker);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 3)
        );

        // Save button
        saveReleaseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveReleaseButton.addClickListener(e -> saveRelease());

        section.add(form, saveReleaseButton);
        return section;
    }

    private VerticalLayout createDocumentTypeSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        section.add(new H3("Create New Document Type"));

        // Configure fields
        documentTypeNameField.setRequired(true);
        documentTypeNameField.setPlaceholder("e.g., Technical Specification, User Manual, API Documentation");
        documentTypeNameField.setWidth("100%");

        // Form layout
        FormLayout form = new FormLayout();
        form.add(documentTypeNameField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        // Save button
        saveDocumentTypeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveDocumentTypeButton.addClickListener(e -> saveDocumentType());

        section.add(form, saveDocumentTypeButton);
        return section;
    }

    private VerticalLayout createProductsGrid() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        section.setSizeFull();

        section.add(new H3("Existing Products & Releases"));

        // Product grid
        productGrid.addColumn(SoftwareProduct::getProductId).setHeader("ID").setWidth("80px");
        productGrid.addColumn(SoftwareProduct::getProductName).setHeader("Product Name").setFlexGrow(2);
        productGrid.addColumn(product ->
                        product.getOwnerTeam() != null ? product.getOwnerTeam().getTeamName() : "N/A")
                .setHeader("Owner Team");
        productGrid.addComponentColumn(this::createProductActions).setHeader("Actions").setWidth("150px");

        productGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        productGrid.setHeight("200px");

        // When product is selected, show its releases
        productGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                loadReleasesForProduct(event.getValue());
            } else {
                releaseGrid.setItems(List.of());
            }
        });

        // Release grid
        releaseGrid.addColumn(SoftwareRelease::getReleaseId).setHeader("ID").setWidth("80px");
        releaseGrid.addColumn(SoftwareRelease::getVersionNumber).setHeader("Version");
        releaseGrid.addColumn(SoftwareRelease::getReleaseDate).setHeader("Release Date");
        releaseGrid.addComponentColumn(this::createReleaseActions).setHeader("Actions").setWidth("100px");

        releaseGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        releaseGrid.setHeight("150px");

        section.add(productGrid, new H3("Releases for Selected Product"), releaseGrid);
        return section;
    }

    private VerticalLayout createDocumentTypesGrid() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        section.setSizeFull();

        section.add(new H3("Existing Document Types"));

        // Document Type grid
        documentTypeGrid.addColumn(DocumentType::getTypeId).setHeader("ID").setWidth("80px");
        documentTypeGrid.addColumn(DocumentType::getTypeName).setHeader("Type Name").setFlexGrow(2);
        documentTypeGrid.addComponentColumn(this::createDocumentTypeActions).setHeader("Actions").setWidth("150px");

        documentTypeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        documentTypeGrid.setHeight("300px");

        section.add(documentTypeGrid);
        return section;
    }

    private HorizontalLayout createProductActions(SoftwareProduct product) {
        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteBtn.addClickListener(e -> deleteProduct(product));
        deleteBtn.getElement().setAttribute("title", "Delete Product");

        return new HorizontalLayout(deleteBtn);
    }

    private HorizontalLayout createReleaseActions(SoftwareRelease release) {
        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteBtn.addClickListener(e -> deleteRelease(release));
        deleteBtn.getElement().setAttribute("title", "Delete Release");

        return new HorizontalLayout(deleteBtn);
    }

    private HorizontalLayout createDocumentTypeActions(DocumentType documentType) {
        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteBtn.addClickListener(e -> deleteDocumentType(documentType));
        deleteBtn.getElement().setAttribute("title", "Delete Document Type");

        return new HorizontalLayout(deleteBtn);
    }

    private void saveProduct() {
        try {
            SoftwareProduct product = new SoftwareProduct();
            productBinder.writeBean(product);

            productRepository.save(product);

            Notification.show("Product '" + product.getProductName() + "' created successfully!",
                            3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            clearProductForm();
            refreshData();

        } catch (ValidationException e) {
            Notification.show("Please fill in all required fields",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void saveRelease() {
        try {
            SoftwareRelease release = new SoftwareRelease();
            releaseBinder.writeBean(release);

            releaseRepository.save(release);

            Notification.show("Release '" + release.getVersionNumber() + "' created successfully!",
                            3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            clearReleaseForm();
            refreshData();

            // Refresh releases grid if a product is selected
            SoftwareProduct selectedProduct = productGrid.asSingleSelect().getValue();
            if (selectedProduct != null) {
                loadReleasesForProduct(selectedProduct);
            }

        } catch (ValidationException e) {
            Notification.show("Please fill in all required fields",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void saveDocumentType() {
        String typeName = documentTypeNameField.getValue();

        if (typeName == null || typeName.trim().isEmpty()) {
            Notification.show("Document type name is required",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        DocumentType documentType = new DocumentType();
        documentType.setTypeName(typeName.trim());

        documentTypeRepository.save(documentType);

        Notification.show("Document Type '" + typeName + "' created successfully!",
                        3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        documentTypeNameField.clear();
        refreshDocumentTypes();
    }

    private void deleteProduct(SoftwareProduct product) {
        try {
            productRepository.delete(product);
            Notification.show("Product deleted", 2000, Notification.Position.TOP_CENTER);
            refreshData();
        } catch (Exception e) {
            Notification.show("Cannot delete product: It may have associated releases or documents",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteRelease(SoftwareRelease release) {
        try {
            releaseRepository.delete(release);
            Notification.show("Release deleted", 2000, Notification.Position.TOP_CENTER);

            SoftwareProduct selectedProduct = productGrid.asSingleSelect().getValue();
            if (selectedProduct != null) {
                loadReleasesForProduct(selectedProduct);
            }
        } catch (Exception e) {
            Notification.show("Cannot delete release: It may have associated documents",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteDocumentType(DocumentType documentType) {
        try {
            documentTypeRepository.delete(documentType);
            Notification.show("Document Type deleted", 2000, Notification.Position.TOP_CENTER);
            refreshDocumentTypes();
        } catch (Exception e) {
            Notification.show("Cannot delete document type: It may have associated documents",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadReleasesForProduct(SoftwareProduct product) {
        List<SoftwareRelease> releases = releaseRepository.findAllWithProduct().stream()
                .filter(r -> r.getProduct() != null && r.getProduct().getProductId() == product.getProductId())
                .toList();
        releaseGrid.setItems(releases);
    }

    private void clearProductForm() {
        productNameField.clear();
        ownerTeamComboBox.clear();
        productBinder.readBean(new SoftwareProduct());
    }

    private void clearReleaseForm() {
        productComboBox.clear();
        versionNumberField.clear();
        releaseDatePicker.setValue(LocalDate.now());
        releaseBinder.readBean(new SoftwareRelease());
    }

    private void refreshData() {
        productGrid.setItems(productRepository.findAllWithOwnerTeam());
        productComboBox.setItems(productRepository.findAll());
        ownerTeamComboBox.setItems(teamRepository.findAll());
        refreshDocumentTypes();
    }

    private void refreshDocumentTypes() {
        documentTypeGrid.setItems(documentTypeRepository.findAll());
    }
}