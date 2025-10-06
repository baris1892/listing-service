package dev.baristop.portfolio.listingservice.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuccessResponse {
    private final String message;
    private final int status;
}
