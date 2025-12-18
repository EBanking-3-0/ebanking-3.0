package com.ebanking.user.api.controller;

import com.ebanking.user.api.dto.UserRequest;
import com.ebanking.user.api.dto.UserResponse;
import com.ebanking.user.application.service.UserService;
import com.ebanking.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User userToCreate = request.toEntity();
        User createdUser = userService.createUser(userToCreate);
        return new ResponseEntity<>(UserResponse.fromEntity(createdUser), HttpStatus.CREATED);
    }
    
    @GetMapping("/test")
    public String testendpoint(){
        return "test successful";
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
    
    
    @PutMapping("/{id}/kyc/{newStatus}")
    public ResponseEntity<UserResponse> updateKycStatus(@PathVariable Long id, @PathVariable User.KycStatus newStatus) {
        User updatedUser = userService.updateKycStatus(id, newStatus);
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}