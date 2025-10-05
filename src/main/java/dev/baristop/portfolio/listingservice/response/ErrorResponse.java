package dev.baristop.portfolio.listingservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

/**
 * Standard structure for API error responses.
 * Includes HTTP status, error reason, message, and timestamp.
 * <p>
 * Usage:
 * ErrorResponse.of(HttpStatus.BAD_REQUEST, "Invalid input")
 * <p>
 * Example JSON response:
 * <pre>
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Invalid input",
 *   "timestamp": 1759664149000
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private Integer status;
    private String error;
    private String message;

    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    // Static factory method for common usage without validation errors
    public static ErrorResponse of(HttpStatus status, String message) {
        return ErrorResponse.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .build();
    }
}
