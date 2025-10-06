package dev.baristop.portfolio.listingservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse extends ErrorResponse {

    @JsonProperty("errors")
    private Map<String, String> validationErrors;

    public ValidationErrorResponse(Integer status, String error, String message, Map<String, String> validationErrors) {
        super(status, error, message, System.currentTimeMillis());

        this.validationErrors = validationErrors;
    }

    public static ValidationErrorResponse of(HttpStatus status, String message, Map<String, String> validationErrors) {
        return new ValidationErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            message,
            validationErrors
        );
    }
}
