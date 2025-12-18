package com.ebanking.user.application.service;

import com.ebanking.user.application.exception.UserNotFoundException;
import com.ebanking.user.domain.model.User;
import com.ebanking.user.domain.repository.UserRepository;
import com.ebanking.user.infrastructure.kafka.UserEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    @Transactional
    public User createUser(User user) {
        // Set timestamps
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save user to database
        User savedUser = userRepository.save(user);

        // Publish rich UserCreatedEvent using shared event class
        userEventProducer.publishUserCreatedEvent(savedUser);

        return savedUser;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User updateKycStatus(Long userId, User.KycStatus newStatus) {
        User user = getUserById(userId);

        // Only proceed if status actually changes
        if (user.getKycStatus() != newStatus) {
            String previousStatus = user.getKycStatus().name(); // Capture before change

            user.setKycStatus(newStatus);
            user.setUpdatedAt(LocalDateTime.now());

            User updatedUser = userRepository.save(user);

            // Publish KycUpdatedEvent with both previous and new status
            userEventProducer.publishKycUpdatedEvent(updatedUser, previousStatus);

            return updatedUser;
        }

        return user;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);

        // In production: consider soft delete (isDeleted flag + anonymization)
        userRepository.delete(user);

        // Publish UserDeletedEvent
        // You can customize the reason based on context (e.g., "user_request", "admin_action", "fraud")
        userEventProducer.publishUserDeletedEvent(user.getId(), "user_deleted");

        // Or if you don't want to track reason:
        // userEventProducer.publishUserDeletedEvent(user.getId());
    }
}