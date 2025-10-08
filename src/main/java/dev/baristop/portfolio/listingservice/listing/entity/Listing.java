package dev.baristop.portfolio.listingservice.listing.entity;

import dev.baristop.portfolio.listingservice.security.dto.UserPrincipal;
import dev.baristop.portfolio.listingservice.security.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
@Setter
@Getter
@NoArgsConstructor
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private BigDecimal price;
    private String city;

    @Enumerated(EnumType.STRING)
    private ListingStatus status = ListingStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFavoriteListing> favorites = new ArrayList<>();

    public Listing(String title, String description, BigDecimal price, String city, ListingStatus status, User owner) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.city = city;
        this.status = status;
        this.owner = owner;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isStatusPending() {
        return this.getStatus().equals(ListingStatus.PENDING);
    }

    public boolean isOwner(User user) {
        return this.getOwner().equals(user);
    }

    public boolean isOwner(UserPrincipal user) {
        return this.getOwner().getKeycloakId().equals(user.id());
    }

    @Override
    public String toString() {
        return "Listing{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", price=" + price +
            ", city='" + city + '\'' +
            ", status=" + status +
            ", owner=" + owner +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }

    public void addFavorite(UserFavoriteListing favorite) {
        // Update the inverse side (Listing's collection)
        this.favorites.add(favorite);

        // Update the owning side (UserFavoriteListing's 'listing' field)
        favorite.setListing(this);
    }
}
