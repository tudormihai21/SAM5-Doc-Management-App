package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.User.Role;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.RoleRepository;
import com.example.docmanagement.Repositories.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Sprint 5: Updated User Management View
 *
 * Now uses MainLayout for consistent navigation drawer.
 * Features:
 * - Create new users with roles
 * - View existing users in a grid
 * - Edit user roles
 * - Delete users
 */
@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Manage Users | DocManagement")
@RolesAllowed("ROLE_ADMIN")
public class UserManagementView extends VerticalLayout {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Form components
    private TextField firstName = new TextField("First Name");
    private TextField lastName = new TextField("Last Name");
    private EmailField email = new EmailField("Email (Username)");
    private PasswordField password = new PasswordField("Password");
    private ComboBox<Role> role = new ComboBox<>("Role");
    private Button saveButton = new Button("Create User", VaadinIcon.PLUS.create());
    private Button clearButton = new Button("Clear", VaadinIcon.ERASER.create());

    // Grid for existing users
    private Grid<User> userGrid = new Grid<>(User.class, false);

    // Binder
    private Binder<User> binder = new Binder<>(User.class);

    // Track if we're editing
    private User editingUser = null;

    public UserManagementView(UserRepository userRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("User Management"));
        add(createUserForm());
        add(new Hr());
        add(createUserGrid());

        refreshGrid();
    }

    private VerticalLayout createUserForm() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        section.add(new H3("Create New User"));

        // Configure fields
        firstName.setRequired(true);
        firstName.setPlaceholder("Enter first name");
        firstName.setWidthFull();

        lastName.setRequired(true);
        lastName.setPlaceholder("Enter last name");
        lastName.setWidthFull();

        email.setRequired(true);
        email.setPlaceholder("user@example.com");
        email.setWidthFull();

        password.setRequired(true);
        password.setPlaceholder("Enter password");
        password.setWidthFull();

        role.setItems(roleRepository.findAll());
        role.setItemLabelGenerator(Role::getRoleName);
        role.setRequired(true);
        role.setPlaceholder("Select role");
        role.setWidthFull();

        // Bind fields
        binder.forField(firstName)
                .asRequired("First name is required")
                .bind(User::getFirstName, User::setFirstName);
        binder.forField(lastName)
                .asRequired("Last name is required")
                .bind(User::getLastName, User::setLastName);
        binder.forField(email)
                .asRequired("Email is required")
                .bind(User::getEmail, User::setEmail);
        binder.forField(role)
                .asRequired("Role is required")
                .bind(User::getRole, User::setRole);

        // Form layout
        FormLayout form = new FormLayout();
        form.add(firstName, lastName, email, password, role);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("800px", 3)
        );
        form.setColspan(email, 2);

        // Buttons
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveUser());

        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.addClickListener(e -> clearForm());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, clearButton);

        section.add(form, buttonLayout);
        return section;
    }

    private VerticalLayout createUserGrid() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        section.setSizeFull();

        section.add(new H3("Existing Users"));

        // Configure grid
        userGrid.addColumn(User::getUserId).setHeader("ID").setWidth("70px").setSortable(true);
        userGrid.addColumn(User::getFirstName).setHeader("First Name").setSortable(true);
        userGrid.addColumn(User::getLastName).setHeader("Last Name").setSortable(true);
        userGrid.addColumn(User::getEmail).setHeader("Email").setFlexGrow(2).setSortable(true);
        userGrid.addColumn(user -> user.getRole() != null ? user.getRole().getRoleName() : "N/A")
                .setHeader("Role").setSortable(true);
        userGrid.addComponentColumn(this::createUserActions).setHeader("Actions").setWidth("150px");

        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        userGrid.setHeight("300px");

        section.add(userGrid);
        return section;
    }

    private HorizontalLayout createUserActions(User user) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setPadding(false);

        // Edit button
        Button editBtn = new Button(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.getElement().setAttribute("title", "Edit User");
        editBtn.addClickListener(e -> editUser(user));

        // Delete button
        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteBtn.getElement().setAttribute("title", "Delete User");
        deleteBtn.addClickListener(e -> confirmDelete(user));

        actions.add(editBtn, deleteBtn);
        return actions;
    }

    private void saveUser() {
        try {
            User user;

            if (editingUser != null) {
                // Editing existing user
                user = editingUser;
                binder.writeBean(user);

                // Only update password if a new one is provided
                if (!password.getValue().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(password.getValue()));
                }

                userRepository.save(user);

                Notification.show("User '" + user.getFirstName() + " " + user.getLastName() + "' updated!",
                                3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                // Creating new user
                user = new User();
                binder.writeBean(user);

                // Password is required for new users
                if (password.getValue().isEmpty()) {
                    Notification.show("Password is required for new users",
                                    3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                user.setPassword(passwordEncoder.encode(password.getValue()));
                userRepository.save(user);

                Notification.show("User '" + user.getFirstName() + " " + user.getLastName() + "' created!",
                                3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            clearForm();
            refreshGrid();

        } catch (Exception e) {
            Notification.show("Error saving user: " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void editUser(User user) {
        editingUser = user;
        binder.readBean(user);
        password.clear();
        password.setPlaceholder("Leave empty to keep current password");
        saveButton.setText("Update User");
        saveButton.setIcon(VaadinIcon.CHECK.create());

        // Scroll to top of form
        firstName.focus();
    }

    private void confirmDelete(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete User");
        dialog.setText("Are you sure you want to delete user '" +
                user.getFirstName() + " " + user.getLastName() + "'? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                userRepository.delete(user);
                Notification.show("User deleted", 2000, Notification.Position.TOP_CENTER);
                refreshGrid();

                // Clear form if we were editing this user
                if (editingUser != null && editingUser.getUserId() == user.getUserId()) {
                    clearForm();
                }
            } catch (Exception ex) {
                Notification.show("Cannot delete user: " + ex.getMessage(),
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void clearForm() {
        editingUser = null;
        binder.readBean(new User());
        firstName.clear();
        lastName.clear();
        email.clear();
        password.clear();
        role.clear();
        password.setPlaceholder("Enter password");
        saveButton.setText("Create User");
        saveButton.setIcon(VaadinIcon.PLUS.create());
    }

    private void refreshGrid() {
        userGrid.setItems(userRepository.findAll());
    }
}