package com.ebanking.assistant.action;

import com.ebanking.assistant.action.actions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActionRegistryTest {
    
    private ActionRegistry actionRegistry;
    
    @BeforeEach
    void setUp() {
        List<ActionExecutor> actions = Arrays.asList(
                new QueryBalanceAction(null),
                new QueryTransactionsAction(null),
                new QueryAccountInfoAction(null),
                new QueryUserInfoAction(null),
                new QueryCryptoPortfolioAction(null)
        );
        actionRegistry = new ActionRegistry(actions);
    }
    
    @Test
    void testActionRegistryLoadsActions() {
        assertNotNull(actionRegistry);
        assertTrue(actionRegistry.hasAction("query_balance"));
        assertTrue(actionRegistry.hasAction("query_transactions"));
        assertTrue(actionRegistry.hasAction("query_account_info"));
        assertTrue(actionRegistry.hasAction("query_user_info"));
        assertTrue(actionRegistry.hasAction("query_crypto_portfolio"));
    }
    
    @Test
    void testGetAction() {
        assertTrue(actionRegistry.getAction("query_balance").isPresent());
        assertTrue(actionRegistry.getAction("query_transactions").isPresent());
        assertFalse(actionRegistry.getAction("unknown_action").isPresent());
    }
}
