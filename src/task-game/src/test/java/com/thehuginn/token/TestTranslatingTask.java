package com.thehuginn.token;

import com.thehuginn.resolution.TokenResolver;
import com.thehuginn.token.unresolved.PlayerUnresolvedToken;
import com.thehuginn.token.unresolved.Token;
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
        List<Token> tokens = TokenResolver.translateTask("This is just a test with {player_1}");
        Assertions.assertEquals(tokens.size(), 1);
        Assertions.assertTrue(tokens.get(0) instanceof PlayerUnresolvedToken);
    }
}
