package dev.baristop.portfolio.listingservice.listing.entity;

import dev.baristop.portfolio.listingservice.security.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
    name = "user_favorite_listing",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "listing_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class UserFavoriteListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UserFavoriteListing(User user, Listing listing) {
        this.user = user;
        this.listing = listing;
    }

    @Override
    public String toString() {
        return "UserFavoriteListing{" +
            "id=" + id +
            ", user=" + user +
            ", listing=" + listing +
            ", createdAt=" + createdAt +
            '}';
    }
}
