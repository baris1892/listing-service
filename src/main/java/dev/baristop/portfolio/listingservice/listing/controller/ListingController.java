package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.security.util.Role;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listings")
@AllArgsConstructor
@Validated
public class ListingController {

    @GetMapping("/test-public")
    public String testPublic() {
        return "Test Public";
    }

    @GetMapping("/test-user")
    @Secured({Role.USER})
    public String testUser() {
        return "Test User";
    }

    @GetMapping("/test-admin")
    @Secured({Role.ADMIN})
    public String testAdmin() {
        return "Test Admin";
    }

}
