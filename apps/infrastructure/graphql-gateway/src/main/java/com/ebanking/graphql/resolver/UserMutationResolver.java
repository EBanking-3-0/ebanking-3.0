package com.ebanking.graphql.resolver;

import com.ebanking.graphql.client.UserServiceClient;
import com.ebanking.graphql.model.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserMutationResolver {

    private final UserServiceClient userServiceClient;

    @MutationMapping
    public UserDTO createUser(@Argument Map<String, Object> input) {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail((String) input.get("email"));
        userDTO.setFirstName((String) input.get("firstName"));
        userDTO.setLastName((String) input.get("lastName"));
        userDTO.setPhone((String) input.get("phone"));
        return userServiceClient.createUser(userDTO);
    }

    @MutationMapping
    public UserDTO updateUser(@Argument Long id, @Argument Map<String, Object> input) {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName((String) input.get("firstName"));
        userDTO.setLastName((String) input.get("lastName"));
        userDTO.setPhone((String) input.get("phone"));
        return userServiceClient.updateUser(id, userDTO);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        userServiceClient.deleteUser(id);
        return true;
    }
}
