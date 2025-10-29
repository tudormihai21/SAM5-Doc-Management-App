package com.example.docmanagement.Domain.Team;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TeamMemberId implements Serializable {

    @Column(name = "UserID")
    private int userId;

    @Column(name = "TeamID")
    private int teamId;

    // Constructori
    public TeamMemberId() {
    }

    public TeamMemberId(int userId, int teamId) {
        this.userId = userId;
        this.teamId = teamId;
    }

    // Getters, Setters, equals() și hashCode() sunt esențiale
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMemberId that = (TeamMemberId) o;
        return userId == that.userId && teamId == that.teamId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, teamId);
    }
}
