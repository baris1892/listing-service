package dev.baristop.portfolio.listingservice.security.entity;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true)
    private String keycloakId;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listing> listings = new ArrayList<>();

    public User() {
    }

    public User(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", keycloakId='" + keycloakId + '\'' +
            '}';
    }
}
