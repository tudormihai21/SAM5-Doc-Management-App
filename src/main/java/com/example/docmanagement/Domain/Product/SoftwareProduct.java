package com.example.docmanagement.Domain.Product;
import jakarta.persistence.*;
import java.util.Set;
// IMPORT CLASA
 import com.example.docmanagement.Domain.Team.Team;

@Entity
@Table(name = "SoftwareProduct")
public class SoftwareProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private int productId;

    @Column(name = "ProductName")
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OwnerTeamID", referencedColumnName = "TeamID")
    private Team ownerTeam;

    @OneToMany(mappedBy = "product")
    private Set<SoftwareRelease> releases;

    public SoftwareProduct() {
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Team getOwnerTeam() {
        return ownerTeam;
    }

    public void setOwnerTeam(Team ownerTeam) {
        this.ownerTeam = ownerTeam;
    }

    public Set<SoftwareRelease> getReleases() {
        return releases;
    }

    public void setReleases(Set<SoftwareRelease> releases) {
        this.releases = releases;
    }
}
