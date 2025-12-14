package com.ebanking.assistant.action;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for all available banking actions.
 * Actions are registered automatically via Spring dependency injection.
 */
@Slf4j
@Component
public class ActionRegistry {
    
    private final Map<String, ActionExecutor> actions = new HashMap<>();
    
    public ActionRegistry(List<ActionExecutor> actionExecutors) {
        for (ActionExecutor executor : actionExecutors) {
            register(executor);
        }
        log.info("Registered {} actions: {}", actions.size(), actions.keySet());
    }
    
    private void register(ActionExecutor executor) {
        String actionName = executor.getActionName();
        if (actions.containsKey(actionName)) {
            log.warn("Action {} is already registered, overwriting", actionName);
        }
        actions.put(actionName, executor);
    }
    
    public Optional<ActionExecutor> getAction(String actionName) {
        return Optional.ofNullable(actions.get(actionName));
    }
    
    public Map<String, ActionExecutor> getAllActions() {
        return new HashMap<>(actions);
    }
    
    public boolean hasAction(String actionName) {
        return actions.containsKey(actionName);
    }
}
