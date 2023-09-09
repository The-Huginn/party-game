package com.thehuginn.resolution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Provider
public class ResolutionContextProvider implements ParamConverterProvider {

    @Inject
    ContainerRequestContext containerRequestContext;

    public static class ResolutionContextConverter implements ParamConverter<ResolutionContext.Builder> {
        private final ContainerRequestContext containerRequestContext;

        public ResolutionContextConverter(ContainerRequestContext containerRequestContext) {
            this.containerRequestContext = containerRequestContext;
        }

        @Override
        public ResolutionContext.Builder fromString(String value) {
            String gameId = containerRequestContext.getCookies().get("gameId").getValue();
            String locale = containerRequestContext.getCookies().containsKey("locale")
                    ? containerRequestContext.getCookies().get("locale").getValue()
                    : "en";
            try {
                JsonNode root = new ObjectMapper().readTree(value);
                if (!root.has("players") || !root.get("players").isArray()) {
                    throw new IllegalArgumentException("Resolution Context can not be created due to missing fields");
                }
                List<String> players = new ArrayList<>();
                root.get("players").elements().forEachRemaining(
                        jsonNode -> players.add(jsonNode.has("name") ? jsonNode.get("name").asText() : jsonNode.asText()));
                return ResolutionContext.builder(gameId)
                        .locale(locale)
                        .players(players);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString(ResolutionContext.Builder value) {
            return null;
        }
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(ResolutionContext.Builder.class)) {
            //noinspection unchecked
            return (ParamConverter<T>) new ResolutionContextConverter(containerRequestContext);
        }
        return null;
    }
}
