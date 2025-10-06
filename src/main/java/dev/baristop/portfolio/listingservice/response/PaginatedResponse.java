package dev.baristop.portfolio.listingservice.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PaginatedResponse<T> {

    private final List<T> data;
    private final Pagination pagination;

    // Constructor for JSON deserialization
    @JsonCreator
    public PaginatedResponse(
        @JsonProperty("data") List<T> data,
        @JsonProperty("pagination") Pagination pagination
    ) {
        this.data = data;
        this.pagination = pagination;
    }

    // Convenience constructor from Page
    public PaginatedResponse(Page<T> page) {
        this.data = page.getContent();
        this.pagination = new Pagination(
            Math.max(page.getNumber() + 1, 1), // converting to 1-based page number
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.isLast()
        );
    }

    public record Pagination(int page, int size, int totalPages, long totalElements, boolean isLast) {
    }
}
