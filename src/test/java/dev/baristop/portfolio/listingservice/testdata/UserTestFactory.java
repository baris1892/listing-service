package dev.baristop.portfolio.listingservice.testdata;

import dev.baristop.portfolio.listingservice.security.dto.UserPrincipal;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserTestFactory {

    @Autowired
    private UserRepository userRepository;

    public User createUser(String id) {
        User user = new User();
        user.setKeycloakId(id);
        user.setEmail(id + "@example.com");

        return userRepository.save(user);
    }

    public User createDefaultUser() {
        return userRepository.findByKeycloakId("test-id")
            .orElseGet(() -> createUser("test-id"));
    }

    public UserPrincipal asPrincipal(User user) {
        return new UserPrincipal(user.getKeycloakId(), user.getEmail(), List.of());
    }
}
