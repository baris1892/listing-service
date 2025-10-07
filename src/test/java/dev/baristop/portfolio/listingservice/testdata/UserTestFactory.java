package dev.baristop.portfolio.listingservice.testdata;

import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserTestFactory {
    @Autowired
    private UserRepository userRepository;

    public User createUser(String id) {
        User user = new User();
        user.setKeycloakId(id);

        return userRepository.save(user);
    }

    public User createDefaultUser() {
        return userRepository.findByKeycloakId("test-id")
            .orElseGet(() -> createUser("test-id"));
    }
}
