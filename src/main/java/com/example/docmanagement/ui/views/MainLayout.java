package com.example.docmanagement.ui.views;

import com.example.docmanagement.Services.Security.SecurityService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;

/**
 * Sprint 5: Updated Main Layout with Full Navigation
 * 
 * Enhanced navigation drawer including:
 * - Document list view
 * - Document upload view
 * - Project dashboard
 * - Project creation (Admin/Manager)
 * - Document status management (Admin/Manager)
 * - Admin menus
 * - Manager menus
 */
public class MainLayout extends AppLayout {

    private final SecurityService securityService;
    private final AuthenticationContext authContext;

    public MainLayout(SecurityService securityService, AuthenticationContext authContext) {
        this.securityService = securityService;
        this.authContext = authContext;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("DocManagement");
        logo.addClassNames("text-l", "m-m");

        Button logoutButton = new Button("Logout", e -> {
            authContext.logout();
        });

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logoutButton);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setPadding(true);
        drawerContent.setSpacing(false);

        // === DOCUMENTS SECTION ===
        Span docSection = new Span("Documents");
        docSection.getStyle().set("font-weight", "bold");
        docSection.getStyle().set("color", "var(--lumo-secondary-text-color)");
        docSection.getStyle().set("font-size", "var(--lumo-font-size-s)");
        drawerContent.add(docSection);

        RouterLink homeLink = new RouterLink("All Documents", DocumentListView.class);
        homeLink.addComponentAsFirst(VaadinIcon.FILE_TEXT.create());
        drawerContent.add(homeLink);

        RouterLink uploadLink = new RouterLink("Upload Document", DocumentUploadView.class);
        uploadLink.addComponentAsFirst(VaadinIcon.UPLOAD.create());
        drawerContent.add(uploadLink);

        drawerContent.add(new Hr());

        // === PROJECTS SECTION ===
        Span projectSection = new Span("Projects");
        projectSection.getStyle().set("font-weight", "bold");
        projectSection.getStyle().set("color", "var(--lumo-secondary-text-color)");
        projectSection.getStyle().set("font-size", "var(--lumo-font-size-s)");
        drawerContent.add(projectSection);

        RouterLink projectLink = new RouterLink("Project Dashboard", ProjectDashboardView.class);
        projectLink.addComponentAsFirst(VaadinIcon.RECORDS.create());
        drawerContent.add(projectLink);

        // Project creation (Admin/Manager only)
        if (securityService.isCurrentUserAdmin() || SecurityService.isCurrentUserProjectManager()) {
            RouterLink createProjectLink = new RouterLink("Create Project", ProjectCreationView.class);
            createProjectLink.addComponentAsFirst(VaadinIcon.PLUS_CIRCLE.create());
            drawerContent.add(createProjectLink);
        }

        drawerContent.add(new Hr());

        // === MANAGEMENT SECTION (Admin/Manager) ===
        if (securityService.isCurrentUserAdmin() || SecurityService.isCurrentUserProjectManager()) {
            Span mgmtSection = new Span("Management");
            mgmtSection.getStyle().set("font-weight", "bold");
            mgmtSection.getStyle().set("color", "var(--lumo-secondary-text-color)");
            mgmtSection.getStyle().set("font-size", "var(--lumo-font-size-s)");
            drawerContent.add(mgmtSection);

            RouterLink statusLink = new RouterLink("Document Status", DocumentStatusManagementView.class);
            statusLink.addComponentAsFirst(VaadinIcon.CHECK_SQUARE.create());
            drawerContent.add(statusLink);

            if (SecurityService.isCurrentUserProjectManager()) {
                RouterLink teamManageLink = new RouterLink("Manage Team", TeamManagementView.class);
                teamManageLink.addComponentAsFirst(VaadinIcon.USER_CHECK.create());
                drawerContent.add(teamManageLink);
            }

            drawerContent.add(new Hr());
        }

        // === ADMIN SECTION ===
        if (securityService.isCurrentUserAdmin()) {
            Span adminSection = new Span("Administration");
            adminSection.getStyle().set("font-weight", "bold");
            adminSection.getStyle().set("color", "var(--lumo-secondary-text-color)");
            adminSection.getStyle().set("font-size", "var(--lumo-font-size-s)");
            drawerContent.add(adminSection);

            RouterLink userAdminLink = new RouterLink("Manage Users", UserManagementView.class);
            userAdminLink.addComponentAsFirst(VaadinIcon.USERS.create());
            drawerContent.add(userAdminLink);

            RouterLink teamAdminLink = new RouterLink("Create Teams", TeamCreationView.class);
            teamAdminLink.addComponentAsFirst(VaadinIcon.GROUP.create());
            drawerContent.add(teamAdminLink);
        }

        addToDrawer(drawerContent);
    }
}
