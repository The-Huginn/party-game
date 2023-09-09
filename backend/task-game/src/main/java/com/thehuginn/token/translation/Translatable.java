package com.thehuginn.token.translation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = Translatable.class)
public interface Translatable {
    String getContent();

    String getLocale();
}
