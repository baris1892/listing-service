package dev.baristop.portfolio.listingservice.listing.controller;

import dev.baristop.portfolio.listingservice.security.WithMockCustomUser;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.util.Role;
import dev.baristop.portfolio.listingservice.testdata.ListingTestFactory;
import dev.baristop.portfolio.listingservice.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserListingControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingTestFactory listingTestFactory;

    @Test
    public void getMyListings_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/listings"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(id = "test-user1", roles = {Role.USER})
    public void getMyListings_shouldReturn200() throws Exception {
        User user = listingTestFactory.createUser("test-user1");
        listingTestFactory.prepareDataForAllListings(user);

        mockMvc.perform(get("/api/v1/users/me/listings"))
            .andExpect(status().isOk())
            // .andDo(JsonTestUtils::printJson)
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(4))
            .andExpect(jsonPath("$.data[0].title").value("Google Pixel 8"))
            .andExpect(jsonPath("$.data[1].title").value("iPhone 14"))
            .andExpect(jsonPath("$.data[2].title").value("Galaxy S23"))
            .andExpect(jsonPath("$.data[3].title").value("Galaxy S22"));
    }
}
