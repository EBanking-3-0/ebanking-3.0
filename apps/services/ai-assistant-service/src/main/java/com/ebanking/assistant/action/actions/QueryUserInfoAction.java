package com.ebanking.assistant.action.actions;

import com.ebanking.assistant.action.ActionExecutionException;
import com.ebanking.assistant.action.ActionExecutor;
import com.ebanking.assistant.client.UserServiceClient;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryUserInfoAction implements ActionExecutor {

  private final UserServiceClient userServiceClient;

  @Override
  public String getActionName() {
    return "query_user_info";
  }

  @Override
  public String getDescription() {
    return "Query user profile information including name, email, and phone. Requires userId or email parameter.";
  }

  @Override
  public Map<String, Object> execute(Long userId, Map<String, Object> parameters)
      throws ActionExecutionException {
    try {
      Object userIdObj = parameters.get("userId");
      Object emailObj = parameters.get("email");

      Map<String, Object> userInfo;

      if (emailObj != null) {
        String email = emailObj.toString();
        log.info("Querying user info by email {} for user {}", email, userId);
        userInfo = userServiceClient.getUserByEmail(email);
      } else if (userIdObj != null) {
        Long targetUserId;
        if (userIdObj instanceof Number) {
          targetUserId = ((Number) userIdObj).longValue();
        } else {
          targetUserId = Long.parseLong(userIdObj.toString());
        }

        log.info("Querying user info for user {}", targetUserId);
        userInfo = userServiceClient.getUserById(targetUserId);
      } else {
        // Default to current user
        log.info("Querying user info for current user {}", userId);
        userInfo = userServiceClient.getUserById(userId);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("user", userInfo);
      result.put("success", true);

      return result;
    } catch (Exception e) {
      log.error("Error querying user info", e);
      throw new ActionExecutionException("Failed to query user info: " + e.getMessage(), e);
    }
  }
}
