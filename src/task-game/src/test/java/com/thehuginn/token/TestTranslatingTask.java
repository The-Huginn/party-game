package com.thehuginn.token;

import com.thehuginn.resolution.TokenResolver;
import com.thehuginn.token.unresolved.PlayerUnresolvedToken;
import com.thehuginn.token.unresolved.TimerUnresolvedToken;
import com.thehuginn.token.unresolved.UnresolvedToken;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTranslatingTask {

    @Test
    @Order(1)
    public void testResolvingSimpleTextTask() {
        Assertions.assertTrue(TokenResolver.translateTask("this is just a test").isEmpty());
    }

    @Test
    @Order(2)
    public void testResolvingTaskWithPlayer() {
        List<UnresolvedToken> tokens = TokenResolver.translateTask("This is just a test with {player_1}");
        Assertions.assertEquals(tokens.size(), 1);
        Assertions.assertTrue(tokens.get(0) instanceof PlayerUnresolvedToken);
        Assertions.assertEquals(((PlayerUnresolvedToken) tokens.get(0)).getKey(), "{player_1}");
    }

    @Test
    @Order(3)
    public void testResolvingTaskWithTimer() {
        List<UnresolvedToken> tokens = TokenResolver.translateTask("This is just a test with {timer_7}");
        Assertions.assertEquals(tokens.size(), 1);
        Assertions.assertTrue(tokens.get(0) instanceof TimerUnresolvedToken);
        Assertions.assertEquals(((TimerUnresolvedToken) tokens.get(0)).getKey(), "{timer_7}");
    }

    @Test
    @Order(6)
    public void testResolvingTaskWithCurrentPlayerOneRandomPlayerOneTimer() {
        List<UnresolvedToken> tokens = TokenResolver.translateTask("{player_c} has to laugh with {player_1} for {timer_42}");
        Assertions.assertEquals(tokens.size(), 3);
        Assertions.assertTrue(tokens.get(0) instanceof PlayerUnresolvedToken);
        Assertions.assertEquals(((PlayerUnresolvedToken) tokens.get(0)).getKey(), "{player_c}");
        Assertions.assertTrue(tokens.get(1) instanceof PlayerUnresolvedToken);
        Assertions.assertEquals(((PlayerUnresolvedToken) tokens.get(1)).getKey(), "{player_1}");
        Assertions.assertTrue(tokens.get(2) instanceof TimerUnresolvedToken);
        Assertions.assertEquals(((TimerUnresolvedToken) tokens.get(2)).getKey(), "{timer_42}");
    }
}
