package dev.baristop.portfolio.listingservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidListingStateException extends BusinessException {
    public InvalidListingStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
