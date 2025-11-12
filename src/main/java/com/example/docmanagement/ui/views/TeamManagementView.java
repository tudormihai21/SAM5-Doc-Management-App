package com.example.docmanagement.ui.views;

import com.example.docmanagement.Domain.Team.Team;
import com.example.docmanagement.Domain.Team.TeamMember;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.TeamMemberRepository;
import com.example.docmanagement.Repositories.TeamRepository;
import com.example.docmanagement.Repositories.TeamRepository;
import com.example.docmanagement.Repositories.UserRepository;
import com.example.docmanagement.Services.Security.SecurityService;
import com.example.docmanagement.ui.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "manager/team", layout = MainLayout.class)
@RolesAllowed("ROLE_PROJECT_MANAGER")
public class TeamManagementView extends VerticalLayout {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SecurityService securityService;

    private Team currentManagerTeam;
    private Grid<User> availableMembersGrid = new Grid<>(User.class);
    private Grid<User> teamMembersGrid = new Grid<>(User.class);
    private Button addButton = new Button(VaadinIcon.ARROW_RIGHT.create());
    private Button removeButton = new Button(VaadinIcon.ARROW_LEFT.create());

    public TeamManagementView(TeamRepository teamRepository, UserRepository userRepository,
                              TeamMemberRepository teamMemberRepository, SecurityService securityService) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.securityService = securityService;


        Optional<User> currentUser = securityService.getAuthenticatedUser();
        if (currentUser.isEmpty()) {
            add(new H2("Eroare: Nu am putut identifica managerul."));
            return;
        }
        Optional<Team> team = teamRepository.findByManager(currentUser.get());
        if (team.isEmpty()) {
            add(new H2("Nu sunteți asignat ca manager la nicio echipă."));
            return;
        }
        this.currentManagerTeam = team.get();

        setSizeFull();
        add(new H2("Gestionează Echipa: " + currentManagerTeam.getTeamName()));

        configureGrids();
        SplitLayout splitLayout = new SplitLayout(
                createGridWrapper("Membri Disponibili", availableMembersGrid, removeButton),
                createGridWrapper("Membrii Echipei Tale", teamMembersGrid, addButton)
        );
        splitLayout.setSizeFull();
        add(splitLayout);

        addButton.addClickListener(e -> addSelectedMember());
        removeButton.addClickListener(e -> removeSelectedMember());

        refreshGrids();
    }

    private void configureGrids() {
        availableMembersGrid.setColumns("firstName", "lastName", "email");
        teamMembersGrid.setColumns("firstName", "lastName", "email");
    }

    private VerticalLayout createGridWrapper(String title, Grid<User> grid, Button button) {
        VerticalLayout wrapper = new VerticalLayout(new H2(title), grid, button);
        wrapper.setSizeFull();
        wrapper.setAlignItems(Alignment.CENTER);
        return wrapper;
    }

    private void refreshGrids() {
        Set<User> membersInTeam = teamMemberRepository.findByTeam(currentManagerTeam)
                .stream()
                .map(TeamMember::getUser)
                .collect(Collectors.toSet());
        teamMembersGrid.setItems(membersInTeam);

        availableMembersGrid.setItems(userRepository.findAvailableTeamMembers());
    }

    private void addSelectedMember() {
        User selectedUser = availableMembersGrid.asSingleSelect().getValue();
        if (selectedUser == null) {
            Notification.show("Selectează un membru disponibil.", 2000, Notification.Position.MIDDLE);
            return;
        }

        TeamMember newMemberLink = new TeamMember();
        newMemberLink.setUser(selectedUser);
        newMemberLink.setTeam(currentManagerTeam);
        teamMemberRepository.save(newMemberLink);

        refreshGrids();
    }

    private void removeSelectedMember() {
        User selectedUser = teamMembersGrid.asSingleSelect().getValue();
        if (selectedUser == null) {
            Notification.show("Selectează un membru din echipa ta.", 2000, Notification.Position.MIDDLE);
            return;
        }

        Optional<TeamMember> memberLink = teamMemberRepository.findByUserAndTeam(selectedUser, currentManagerTeam);
        memberLink.ifPresent(teamMemberRepository::delete);

        refreshGrids();
    }
}