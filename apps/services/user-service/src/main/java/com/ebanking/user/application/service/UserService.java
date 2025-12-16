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
        // 1. Sauvegarde dans la base de données
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        
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
        
        if (user.getKycStatus() != newStatus) {
            user.setKycStatus(newStatus);
            user.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);

            // 3. Publier l'événement de mise à jour KYC
            userEventProducer.publishKycUpdatedEvent(updatedUser);
            return updatedUser;
        }
        return user;
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        
       // Dans un vrai système, on ferait souvent une désactivation et/ou une anonymisation.
        userRepository.delete(user);
        
        // 4. Publier l'événement de suppression/anonymisation
        userEventProducer.publishUserDeletedEvent(user.getId());
    }
}