package com.ebanking.assistant.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
}
