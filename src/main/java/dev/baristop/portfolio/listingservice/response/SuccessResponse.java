package dev.baristop.portfolio.listingservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class SuccessResponse {
    private String message;
    private final int status;
}
