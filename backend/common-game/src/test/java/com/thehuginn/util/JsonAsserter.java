package com.thehuginn.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;

public class JsonAsserter {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void assertEquals(Object obj1, Object obj2) {
        try {
            Assertions.assertEquals(mapper.readTree(obj1.toString()), mapper.readTree(obj2.toString()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
