package com.example.docmanagement.Domain.User;
import jakarta.persistence.*;
import java.util.Set;
// IMPORT CLASE
// DOCUMENT & TEAMMEMBER
import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Team.TeamMember;
@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private int userId;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoleID", referencedColumnName = "RoleID")
    private Role role;

    @OneToMany(mappedBy = "user")
    private Set<TeamMember> teamMembers;

    @OneToMany(mappedBy = "uploader")
    private Set<Document> uploadedDocuments;

    public User() {
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<TeamMember> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(Set<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public Set<Document> getUploadedDocuments() {
        return uploadedDocuments;
    }

    public void setUploadedDocuments(Set<Document> uploadedDocuments) {
        this.uploadedDocuments = uploadedDocuments;
    }
}

