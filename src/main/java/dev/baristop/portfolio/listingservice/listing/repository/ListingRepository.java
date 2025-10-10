package dev.baristop.portfolio.listingservice.listing.repository;

import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    Optional<Listing> findByTitleAndDescriptionAndCity(String title, String description, String city);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE Listing l
            SET l.status = :newStatus,
                l.updatedAt = :now
            WHERE l.status = :currentStatus
              AND l.createdAt < :threshold
        """)
    int updateStatusOlderThan(
        @Param("threshold") Instant threshold,
        @Param("currentStatus") ListingStatus currentStatus,
        @Param("newStatus") ListingStatus newStatus,
        @Param("now") Instant now
    );
}
