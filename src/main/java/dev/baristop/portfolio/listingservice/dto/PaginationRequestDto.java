package dev.baristop.portfolio.listingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaginationRequestDto {

    @Schema(description = "Page number (1-based)", example = "1")
    @Min(1)
    private Integer page = 1;

    @Schema(description = "Page size", example = "10")
    @Min(1)
    @Max(100)
    private Integer size = 10;

    @Schema(description = "Field to sort by", example = "id")
    private String sortBy = "id";

    @Schema(description = "Sort direction", example = "asc")
    private String sortDir = "asc";

    public int calculateZeroBasedPage() {
        return Math.max(this.getPage() - 1, 0);
    }
}
