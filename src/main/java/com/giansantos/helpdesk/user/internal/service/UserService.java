package com.giansantos.helpdesk.user.internal.service;

import com.giansantos.helpdesk.user.api.CreateUserRequest;
import com.giansantos.helpdesk.user.api.UserQueryApi;
import com.giansantos.helpdesk.user.api.UserSummary;
import com.giansantos.helpdesk.user.internal.domain.User;
import com.giansantos.helpdesk.user.internal.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService implements UserQueryApi {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Ja existe um usuario com o e-mail " + request.email());
        }
        User user = User.create(request.email(), request.displayName(), request.role());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSummary> findSummary(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> new UserSummary(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole()));
    }
}
