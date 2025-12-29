package com.ebanking.user.api.controller;

import com.ebanking.shared.dto.UserRequest;
import com.ebanking.shared.dto.UserResponse;
import com.ebanking.user.application.service.UserService;
import com.ebanking.user.domain.model.User;
import com.ebanking.user.api.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        // Map shared DTO to domain entity using mapper
        User userToCreate = userMapper.toEntity(request);

        User createdUser = userService.createUser(userToCreate);

        // Map domain entity to shared DTO using mapper
        UserResponse response = userMapper.toResponse(createdUser);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/test")
    public String testendpoint(){
        return "test successful";
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserResponse response = userMapper.toResponse(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/kyc/{newStatus}")
    public ResponseEntity<UserResponse> updateKycStatus(@PathVariable Long id, @PathVariable User.KycStatus newStatus) {
        User updatedUser = userService.updateKycStatus(id, newStatus);
        UserResponse response = userMapper.toResponse(updatedUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}