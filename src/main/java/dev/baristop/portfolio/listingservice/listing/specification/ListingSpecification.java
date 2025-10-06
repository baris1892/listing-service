package dev.baristop.portfolio.listingservice.listing.specification;

import dev.baristop.portfolio.listingservice.listing.dto.ListingQueryRequestDto;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.Listing_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A utility class for creating dynamic JPA specifications for filtering <code>Listing</code> entities.
 */
@Slf4j
public class ListingSpecification {

    /**
     * Creates a JPA {@link Specification} for filtering {@link Listing} entities based on the provided criteria.
     *
     * <p>The filter supports the following optional fields:
     * <ul>
     *   <li><code>title</code>: filters listings by title (case-insensitive, partial match)</li>
     *   <li><code>description</code>: filters listings by description (case-insensitive, partial match)</li>
     *   <li><code>city</code>: filters listings by city (case-insensitive, partial match)</li>
     * </ul>
     *
     * <p>Note: Uses case-insensitive matching for <code>name</code> and <code>email</code> fields
     * by converting to lowercase and using SQL LIKE operator with wildcards.
     *
     * @param requestDto the filter criteria encapsulated in a {@link ListingQueryRequestDto} object
     *
     * @return a {@link Specification} of {@link Listing} that can be used with Spring Data JPA repositories
     */
    public static Specification<Listing> withFilters(ListingQueryRequestDto requestDto) {
        log.debug("Listing filters applied: {}", requestDto);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // basic filters
            if (hasText(requestDto.getTitle())) {
                predicates.add(
                    ilike(cb, root.get(Listing_.title), requestDto.getTitle())
                );
            }

            if (hasText(requestDto.getDescription())) {
                predicates.add(
                    ilike(cb, root.get(Listing_.description), requestDto.getDescription())
                );
            }

            if (hasText(requestDto.getCity())) {
                predicates.add(ilike(cb, root.get(Listing_.city), requestDto.getCity()));
            }

            if (requestDto.getPriceFrom() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(root.get(Listing_.price), requestDto.getPriceFrom())
                );
            }

            if (requestDto.getPriceTo() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(root.get(Listing_.price), requestDto.getPriceTo())
                );
            }

            if (requestDto.getStatus() != null) {
                predicates.add(
                    cb.equal(root.get(Listing_.status), requestDto.getStatus())
                );
            }

            if (requestDto.getUser() != null) {
                predicates.add(
                    cb.equal(root.get(Listing_.owner), requestDto.getUser())
                );
            }

            // avoid duplicates caused by joins
            Objects.requireNonNull(query, "query must not be null").distinct(true);

            return predicates.isEmpty()
                ? cb.conjunction()
                : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // helper: null/blank-safe checker
    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    // helper: case-insensitive LIKE with wildcards
    private static Predicate ilike(CriteriaBuilder cb, Expression<String> expr, String value) {
        return cb.like(
            cb.lower(expr),
            "%" + value.toLowerCase() + "%"
        );
    }
}
