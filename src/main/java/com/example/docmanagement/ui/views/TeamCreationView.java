package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.Team.Team;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.TeamRepository;
import com.example.docmanagement.Repositories.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import com.example.docmanagement.ui.views.MainLayout; // Importăm layout-ul

import java.util.List;

@Route(value = "admin/teams", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class TeamCreationView extends VerticalLayout {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    private H2 title = new H2("Creează Echipă Nouă");
    private TextField teamName = new TextField("Nume Echipă");
    private ComboBox<User> manager = new ComboBox<>("Asignează Manager");
    private Button saveButton = new Button("Salvează Echipa");

    private Binder<Team> binder = new Binder<>(Team.class);

    public TeamCreationView(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;

        List<User> potentialManagers = userRepository.findByRole_RoleName("PROJECT_MANAGER");
        manager.setItems(potentialManagers);
        manager.setItemLabelGenerator(user -> user.getFirstName() + " " + user.getLastName());

        binder.forField(teamName).asRequired("Numele este obligatoriu").bind(Team::getTeamName, Team::setTeamName);
        binder.forField(manager).asRequired("Managerul este obligatoriu").bind(Team::getManager, Team::setManager);

        saveButton.addClickListener(e -> onSave());

        FormLayout formLayout = new FormLayout(teamName, manager);
        add(title, formLayout, saveButton);
    }

    private void onSave() {
        try {
            Team newTeam = new Team();
            binder.writeBean(newTeam);
            teamRepository.save(newTeam);

            Notification.show("Echipa '" + newTeam.getTeamName() + "' a fost creată.", 3000, Notification.Position.MIDDLE);
            binder.readBean(new Team());
        } catch (Exception e) {
            Notification.show("Eroare: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }
}