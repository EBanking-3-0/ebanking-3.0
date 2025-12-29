package com.ebanking.assistant.service;

import com.ebanking.assistant.action.ActionExecutionException;
import com.ebanking.assistant.action.ActionExecutor;
import com.ebanking.assistant.action.ActionRegistry;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionExecutorService {

  private final ActionRegistry actionRegistry;

  public Map<String, Object> executeAction(
      String actionName, Long userId, Map<String, Object> parameters)
      throws ActionExecutionException {

    ActionExecutor executor =
        actionRegistry
            .getAction(actionName)
            .orElseThrow(() -> new ActionExecutionException("Unknown action: " + actionName));

    // Check authorization
    if (!executor.isAuthorized(userId, parameters)) {
      throw new ActionExecutionException(
          "User " + userId + " is not authorized to execute action: " + actionName);
    }

    log.info("Executing action {} for user {} with parameters {}", actionName, userId, parameters);

    try {
      Map<String, Object> result = executor.execute(userId, parameters);
      log.info("Action {} executed successfully for user {}", actionName, userId);
      return result;
    } catch (ActionExecutionException e) {
      log.error("Action execution failed: {}", e.getMessage());
      throw e;
    }
  }
}
