package com.ebanking.assistant.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SecurityUtilTest {

  @Mock private HttpServletRequest request;

  private SecurityUtil securityUtil;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    securityUtil = new SecurityUtil();
  }

  @Test
  void testExtractUserIdFromHeader() {
    when(request.getHeader("X-User-Id")).thenReturn("d81804c0-1e7f-4ee0-8d94-2b6d39e0bf08");

    String userId = securityUtil.extractUserIdFromHeader(request);

    assertEquals("d81804c0-1e7f-4ee0-8d94-2b6d39e0bf08", userId);
  }

  @Test
  void testExtractUserIdFromHeaderInvalid() {
    when(request.getHeader("X-User-Id")).thenReturn(null);
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");

    String userId = securityUtil.extractUserIdFromHeader(request);

    assertNull(userId);
  }

  @Test
  void testExtractUserIdFromHeaderMissing() {
    when(request.getHeader("X-User-Id")).thenReturn(null);
    when(request.getHeader("Authorization")).thenReturn(null);

    String userId = securityUtil.extractUserIdFromHeader(request);

    assertNull(userId);
  }

  @Test
  void testExtractUserIdFromJwtUserIdClaimAsString() {
    when(request.getHeader("X-User-Id")).thenReturn(null);
    when(request.getHeader("Authorization"))
        .thenReturn("Bearer " + buildJwt("{\"sub\":\"b5d8c0a1-2e3f-4ee0-9d94-3b6d39e0bf09\"}"));

    String userId = securityUtil.extractUserIdFromHeader(request);

    assertEquals("b5d8c0a1-2e3f-4ee0-9d94-3b6d39e0bf09", userId);
  }

  @Test
  void testExtractUserIdFromJwtSubClaimAsString() {
    when(request.getHeader("X-User-Id")).thenReturn(null);
    when(request.getHeader("Authorization"))
        .thenReturn("Bearer " + buildJwt("{\"sub\":\"c7e9f1b2-4a5d-4ee0-8d94-5c7e49f0cf10\"}"));

    String userId = securityUtil.extractUserIdFromHeader(request);

    assertEquals("c7e9f1b2-4a5d-4ee0-8d94-5c7e49f0cf10", userId);
  }

  @Test
  void testExtractUserIdFromJwtMissingClaims() {
    when(request.getHeader("X-User-Id")).thenReturn(null);
    when(request.getHeader("Authorization")).thenReturn("Bearer " + buildJwt("{\"foo\":\"bar\"}"));

    String userId = securityUtil.extractUserIdFromHeader(request);

    assertNull(userId);
  }

  private String buildJwt(String payloadJson) {
    String header =
        Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes());
    String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
    return header + "." + payload + ".signature";
  }
}
