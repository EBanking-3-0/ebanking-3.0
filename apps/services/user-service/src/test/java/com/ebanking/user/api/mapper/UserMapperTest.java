package com.ebanking.user.api.mapper;

import com.ebanking.shared.dto.UserRequest;
import com.ebanking.shared.dto.UserResponse;
import com.ebanking.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    public void testToEntityAndToResponse() {
        UserRequest req = new UserRequest();
        req.setEmail("test@example.com");
        req.setUsername("tester");
        req.setFirstName("Test");
        req.setLastName("User");
        req.setPhone("123456789");
        req.setRgpdConsent(true);

        User entity = mapper.toEntity(req);
        assertNotNull(entity);
        assertEquals(req.getEmail(), entity.getEmail());
        assertEquals(req.getUsername(), entity.getUsername());
        assertEquals(req.getFirstName(), entity.getFirstName());
        assertEquals(req.getLastName(), entity.getLastName());
        assertEquals(req.getPhone(), entity.getPhone());
        assertTrue(entity.isRgpdConsent());
        assertNotNull(entity.getStatus());
        assertNotNull(entity.getKycStatus());

        // convert back
        // set an id and createdAt for response mapping
        entity.setId(42L);
        UserResponse resp = mapper.toResponse(entity);
        assertNotNull(resp);
        assertEquals(entity.getId(), resp.getId());
        assertEquals(entity.getEmail(), resp.getEmail());
        assertEquals(entity.getFirstName(), resp.getFirstName());
        assertEquals(entity.getLastName(), resp.getLastName());
        assertEquals(entity.getPhone(), resp.getPhone());
        assertEquals(entity.getStatus().name(), resp.getStatus());
        assertEquals(entity.getKycStatus().name(), resp.getKycStatus());
    }
}

