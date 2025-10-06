package dev.baristop.portfolio.listingservice.security.service;

import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getOrCreateUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
            .orElseGet(() -> {
                User user = new User();
                user.setKeycloakId(keycloakId);

                return userRepository.save(user);
            });
    }
}
