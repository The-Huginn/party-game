package com.thehuginn.common.services.exposed.resolution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Provider
public class ResolutionContextProvider implements ParamConverterProvider {

    @Inject
    ContainerRequestContext containerRequestContext;

    static final Logger LOGGER = Logger.getLogger(ResolutionContextProvider.class);

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
                        jsonNode -> players.add(jsonNode.asText()));

                if (players.isEmpty()) {
                    LOGGER.warn("Received no players for this game");
                }

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
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(ResolutionContext.Builder.class)) {
            return (ParamConverter<T>) new ResolutionContextConverter(containerRequestContext);
        }
        return null;
    }
}
