package com.ebanking.assistant.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SecurityUtilTest {
    
    @Mock
    private HttpServletRequest request;
    
    private SecurityUtil securityUtil;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityUtil = new SecurityUtil();
    }
    
    @Test
    void testExtractUserIdFromHeader() {
        when(request.getHeader("X-User-Id")).thenReturn("123");
        
        Long userId = securityUtil.extractUserIdFromHeader(request);
        
        assertEquals(123L, userId);
    }
    
    @Test
    void testExtractUserIdFromHeaderInvalid() {
        when(request.getHeader("X-User-Id")).thenReturn("invalid");
        
        Long userId = securityUtil.extractUserIdFromHeader(request);
        
        assertNull(userId);
    }
    
    @Test
    void testExtractUserIdFromHeaderMissing() {
        when(request.getHeader("X-User-Id")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);
        
        Long userId = securityUtil.extractUserIdFromHeader(request);
        
        assertNull(userId);
    }

    @Test
    void testExtractUserIdFromJwtUserIdClaimAsString() {
        when(request.getHeader("X-User-Id")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + buildJwt("{\"userId\":\"456\"}"));

        Long userId = securityUtil.extractUserIdFromHeader(request);

        assertEquals(456L, userId);
    }

    @Test
    void testExtractUserIdFromJwtSubClaimAsString() {
        when(request.getHeader("X-User-Id")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + buildJwt("{\"sub\":\"789\"}"));

        Long userId = securityUtil.extractUserIdFromHeader(request);

        assertEquals(789L, userId);
    }

    @Test
    void testExtractUserIdFromJwtMissingClaims() {
        when(request.getHeader("X-User-Id")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + buildJwt("{\"foo\":\"bar\"}"));

        Long userId = securityUtil.extractUserIdFromHeader(request);

        assertNull(userId);
    }

    private String buildJwt(String payloadJson) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());
        return header + "." + payload + ".signature";
    }
}
