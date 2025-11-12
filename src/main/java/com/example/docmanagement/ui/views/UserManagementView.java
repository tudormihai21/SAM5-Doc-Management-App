package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.User.Role;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.RoleRepository;
import com.example.docmanagement.Repositories.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route("admin/users")
@RolesAllowed("ROLE_ADMIN")
public class UserManagementView extends VerticalLayout {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Componentele formularului
    private H2 title = new H2("Creează Cont Utilizator");
    private TextField firstName = new TextField("Prenume");
    private TextField lastName = new TextField("Nume de familie");
    private EmailField email = new EmailField("Email (Username)");
    private PasswordField password = new PasswordField("Parolă");
    private ComboBox<Role> role = new ComboBox<>("Rol");

    private Button saveButton = new Button("Salvează Utilizator");

    private Binder<User> binder = new Binder<>(User.class);

    public UserManagementView(UserRepository userRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;

        FormLayout formLayout = new FormLayout();
        formLayout.add(firstName, lastName, email, password, role);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(email, 2);

        configureFields();

        add(title, formLayout, saveButton);

        saveButton.addClickListener(e -> onSave());
    }

    private void configureFields() {
        role.setItems(roleRepository.findAll());
        role.setItemLabelGenerator(Role::getRoleName);

        binder.forField(firstName).asRequired("Prenumele este obligatoriu").bind(User::getFirstName, User::setFirstName);
        binder.forField(lastName).asRequired("Numele este obligatoriu").bind(User::getLastName, User::setLastName);
        binder.forField(email).asRequired("Email-ul este obligatoriu").bind(User::getEmail, User::setEmail);
        binder.forField(role).asRequired("Rolul este obligatoriu").bind(User::getRole, User::setRole);
    }

    private void onSave() {
        try {
            User newUser = new User();

            binder.writeBean(newUser);

            if (password.getValue().isEmpty()) {
                Notification.show("Parola este obligatorie", 3000, Notification.Position.MIDDLE);
                return;
            }
            newUser.setPassword(passwordEncoder.encode(password.getValue()));

            userRepository.save(newUser);

            Notification.show("Utilizator " + newUser.getFirstName() + " creat!", 3000, Notification.Position.MIDDLE);

            binder.readBean(new User());
            password.clear();

        } catch (Exception e) {

            Notification.show("Eroare la salvare: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }
}