package com.thehuginn.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehuginn.entities.GameContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class GameContextProvider implements ParamConverterProvider {

    @Inject
    ContainerRequestContext containerRequestContext;

    public static class ResolutionContextConverter implements ParamConverter<GameContext> {
        private final ContainerRequestContext containerRequestContext;

        public ResolutionContextConverter(ContainerRequestContext containerRequestContext) {
            this.containerRequestContext = containerRequestContext;
        }

        @Override
        public GameContext fromString(String value) {
            return null;
        }

        @Override
        public String toString(GameContext value) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(GameContext.class)) {
            //noinspection unchecked
            return (ParamConverter<T>) new ResolutionContextConverter(containerRequestContext);
        }
        return null;
    }
}
