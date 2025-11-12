package com.example.docmanagement.ui.views;

import com.example.docmanagement.Services.Security.SecurityService;
import com.example.docmanagement.ui.views.DocumentListView;
import com.example.docmanagement.ui.views.ProjectDashboardView;
import com.example.docmanagement.ui.views.TeamCreationView;
import com.example.docmanagement.ui.views.UserManagementView;
import com.example.docmanagement.ui.views.TeamManagementView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;

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
        RouterLink homeLink = new RouterLink("Documente", DocumentListView.class);
        homeLink.addComponentAsFirst(VaadinIcon.FILE_TEXT.create());

        RouterLink projectLink = new RouterLink("Proiecte", ProjectDashboardView.class);
        projectLink.addComponentAsFirst(VaadinIcon.RECORDS.create());

        VerticalLayout adminMenu = createAdminMenu();
        VerticalLayout managerMenu = createManagerMenu();

        addToDrawer(new VerticalLayout(
                homeLink,
                projectLink,
                adminMenu,
                managerMenu
        ));
    }

    private VerticalLayout createAdminMenu() {

        VerticalLayout adminLayout = new VerticalLayout();
        adminLayout.setPadding(false);
        adminLayout.setSpacing(false);

        RouterLink userAdminLink = new RouterLink("Gestionează Useri", UserManagementView.class);
        userAdminLink.addComponentAsFirst(VaadinIcon.USERS.create());

        RouterLink teamAdminLink = new RouterLink("Creează Echipe", TeamCreationView.class);
        teamAdminLink.addComponentAsFirst(VaadinIcon.GROUP.create());

        adminLayout.add(userAdminLink, teamAdminLink);

        adminLayout.setVisible(securityService.isCurrentUserAdmin());
        return adminLayout;
    }

    private VerticalLayout createManagerMenu() {
        VerticalLayout managerLayout = new VerticalLayout();
        managerLayout.setPadding(false);
        managerLayout.setSpacing(false);

        RouterLink teamManageLink = new RouterLink("Gestionează Echipa", TeamManagementView.class);
        teamManageLink.addComponentAsFirst(VaadinIcon.USER_CHECK.create());

        managerLayout.add(teamManageLink);

        managerLayout.setVisible(securityService.isCurrentUserProjectManager());
        return managerLayout;
    }
}