package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Domain.Team.Team;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.SoftwareProductRepository;
import com.example.docmanagement.Services.DocumentAccess.DocumentAccessService;
import com.example.docmanagement.Services.Security.SecurityService;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sprint 5: Project Dashboard View with Team-Based Filtering
 *
 * Access Control:
 * - ADMIN: Sees ALL projects
 * - PROJECT_MANAGER: Sees ALL projects
 * - TEAM_MEMBER: Sees only projects owned by their team(s)
 */
@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Project Dashboard | DocManagement")
@RolesAllowed({"ROLE_ADMIN", "ROLE_PROJECT_MANAGER", "ROLE_TEAM_MEMBER"})
public class ProjectDashboardView extends VerticalLayout {

    private final DocumentRepository documentRepository;
    private final SoftwareProductRepository productRepository;
    private final DocumentAccessService documentAccessService;

    private ComboBox<SoftwareProduct> projectSelector = new ComboBox<>("Select Project");
    private Grid<Document> documentGrid = new Grid<>(Document.class, false); // false = don't auto-create columns
    private Span accessInfoLabel = new Span();

    public ProjectDashboardView(DocumentRepository documentRepository,
                                SoftwareProductRepository productRepository,
                                DocumentAccessService documentAccessService) {
        this.documentRepository = documentRepository;
        this.productRepository = productRepository;
        this.documentAccessService = documentAccessService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        add(createHeader());

        // Project selector - only shows accessible products
        configureProjectSelector();
        add(projectSelector);

        // Document grid
        configureGrid();
        add(documentGrid);

        projectSelector.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                updateGrid(event.getValue());
            } else {
                documentGrid.setItems(Collections.emptyList());
            }
        });

        // Auto-select first product if available
        List<SoftwareProduct> products = documentAccessService.getAccessibleProducts();
        if (!products.isEmpty()) {
            projectSelector.setValue(products.get(0));
        }
    }

    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);

        H2 title = new H2("Project Dashboard");
        title.getStyle().set("margin", "0");

        // Show access level info
        updateAccessInfoLabel();
        accessInfoLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        accessInfoLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");

        header.add(title, accessInfoLabel);
        return header;
    }

    private void updateAccessInfoLabel() {
        Optional<User> currentUser = SecurityService.getAuthenticatedUser();
        if (currentUser.isEmpty()) {
            accessInfoLabel.setText("");
            return;
        }

        User user = currentUser.get();
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";

        if ("ADMIN".equals(roleName)) {
            accessInfoLabel.setText("Viewing: All projects (Admin access)");
            accessInfoLabel.getElement().getThemeList().add("badge success");
        } else if ("PROJECT_MANAGER".equals(roleName)) {
            accessInfoLabel.setText("Viewing: All projects (Manager access)");
            accessInfoLabel.getElement().getThemeList().add("badge success");
        } else if ("TEAM_MEMBER".equals(roleName)) {
            Set<Team> userTeams = documentAccessService.getUserTeams(user);
            if (userTeams.isEmpty()) {
                accessInfoLabel.setText("⚠ You are not assigned to any team. No projects visible.");
                accessInfoLabel.getElement().getThemeList().add("badge error");
            } else {
                String teamNames = userTeams.stream()
                        .map(Team::getTeamName)
                        .collect(Collectors.joining(", "));
                accessInfoLabel.setText("Viewing: Projects for team(s): " + teamNames);
                accessInfoLabel.getElement().getThemeList().add("badge");
            }
        }
    }

    private void configureProjectSelector() {
        // Get only accessible products based on user role and team membership
        List<SoftwareProduct> accessibleProducts = documentAccessService.getAccessibleProducts();

        projectSelector.setItems(accessibleProducts);
        projectSelector.setItemLabelGenerator(SoftwareProduct::getProductName);
        projectSelector.setWidth("50%");
        projectSelector.setPlaceholder("Select a project to view documents");

        if (accessibleProducts.isEmpty()) {
            projectSelector.setEnabled(false);
            projectSelector.setPlaceholder("No projects available");
        }
    }

    private void configureGrid() {
        documentGrid.setSizeFull();
        documentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Manually add columns to avoid any auto-generation issues
        documentGrid.addColumn(Document::getTitle)
                .setHeader("Title")
                .setSortable(true)
                .setFlexGrow(2);

        documentGrid.addColumn(Document::getDocumentVersion)
                .setHeader("Version")
                .setSortable(true)
                .setWidth("100px");

        documentGrid.addColumn(doc -> doc.getStatus() != null ? doc.getStatus().name() : "N/A")
                .setHeader("Status")
                .setSortable(true)
                .setWidth("120px");

        documentGrid.addColumn(doc -> doc.getUploadTimestamp() != null
                        ? doc.getUploadTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : "N/A")
                .setHeader("Upload Timestamp")
                .setSortable(true)
                .setWidth("160px");

        documentGrid.addColumn(doc ->
                        doc.getUploader() != null
                                ? doc.getUploader().getFirstName() + " " + doc.getUploader().getLastName()
                                : "N/A")
                .setHeader("Uploader")
                .setSortable(true);

        documentGrid.addColumn(doc ->
                        doc.getDocumentType() != null
                                ? doc.getDocumentType().getTypeName()
                                : "N/A")
                .setHeader("Type")
                .setSortable(true);

        documentGrid.addColumn(doc ->
                        doc.getSoftwareRelease() != null
                                ? doc.getSoftwareRelease().getVersionNumber()
                                : "N/A")
                .setHeader("Release")
                .setSortable(true);

        documentGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void updateGrid(SoftwareProduct product) {
        // Double-check access (defense in depth)
        Optional<User> currentUser = SecurityService.getAuthenticatedUser();
        if (currentUser.isEmpty()) {
            documentGrid.setItems(Collections.emptyList());
            return;
        }

        // For team members, verify they have access to this product
        User user = currentUser.get();
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";

        if ("TEAM_MEMBER".equals(roleName)) {
            Set<Team> userTeams = documentAccessService.getUserTeams(user);
            // Compare by team ID to avoid proxy comparison issues
            Set<Integer> userTeamIds = userTeams.stream()
                    .map(Team::getTeamId)
                    .collect(Collectors.toSet());

            if (product.getOwnerTeam() == null || !userTeamIds.contains(product.getOwnerTeam().getTeamId())) {
                // User doesn't have access to this product
                documentGrid.setItems(Collections.emptyList());
                return;
            }
        }

        List<Document> documents = documentRepository.findByProductIdWithDetails(product.getProductId());
        documentGrid.setItems(documents);
    }
}