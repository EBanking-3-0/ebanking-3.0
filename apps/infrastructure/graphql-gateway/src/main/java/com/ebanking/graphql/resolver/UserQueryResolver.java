package com.ebanking.graphql.resolver;

import com.ebanking.graphql.client.UserServiceClient;
import com.ebanking.graphql.model.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserQueryResolver {

    private final UserServiceClient userServiceClient;

    @QueryMapping
    public List<UserDTO> users() {
        return userServiceClient.getAllUsers();
    }

    @QueryMapping
    public UserDTO user(@Argument Long id) {
        return userServiceClient.getUserById(id);
    }

    @QueryMapping
    public UserDTO userByEmail(@Argument String email) {
        return userServiceClient.getUserByEmail(email);
    }
}
