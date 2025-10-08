package dev.baristop.portfolio.listingservice.security.service;

import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateUserByKeycloakId(String keycloakId, String email) {
        return userRepository.findByKeycloakId(keycloakId)
            .orElseGet(() -> {
                User user = new User();
                user.setKeycloakId(keycloakId);
                user.setEmail(email);

                return userRepository.save(user);
            });
    }
}
