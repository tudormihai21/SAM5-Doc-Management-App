package com.example.docmanagement.Domain.Team;
import jakarta.persistence.*;
import java.util.Set;
// IMPORT CLASA
//Software Product
import com.example.docmanagement.Domain.Product.SoftwareProduct;

@Entity
@Table(name = "Team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TeamID")
    private int teamId;

    @Column(name = "TeamName")
    private String teamName;

    @OneToMany(mappedBy = "team")
    private Set<TeamMember> teamMembers;

    @OneToMany(mappedBy = "ownerTeam")
    private Set<SoftwareProduct> ownedProducts;


    public Team() {
    }


    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Set<TeamMember> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(Set<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public Set<SoftwareProduct> getOwnedProducts() {
        return ownedProducts;
    }

    public void setOwnedProducts(Set<SoftwareProduct> ownedProducts) {
        this.ownedProducts = ownedProducts;
    }
}