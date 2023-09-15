package com.thehuginn.token.translation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

@JsonSerialize(as = Translatable.class)
public interface Translatable {
    Map<String, String> getContent();

    String getLocale();
}
