package com.example.docmanagement.Domain.Team;
import jakarta.persistence.*;
// IMPORT CLASA
import com.example.docmanagement.Domain.User.User;
@Entity
@Table(name = "TeamMember")
public class TeamMember {

    @EmbeddedId
    private TeamMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // Leagă atributul 'userId' din TeamMemberId
    @JoinColumn(name = "UserID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId") // Leagă atributul 'teamId' din TeamMemberId
    @JoinColumn(name = "TeamID")
    private Team team;

    // Constructori
    public TeamMember() {
    }

    // Getters and Setters
    public TeamMemberId getId() {
        return id;
    }

    public void setId(TeamMemberId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}