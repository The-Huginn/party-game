package com.thehuginn.token.translation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public interface Translatable {

    @JsonIgnore
    Map<String, String> getContent();

    @JsonIgnore
    String getLocale();
}
