package com.thehuginn.token.translation;

public interface TranslatableCategory extends Translatable {
    static final String NAME_TAG = "name";
    static final String DESCRIPTION_TAG = "description";

    default String getName() {
        return getContent().get(NAME_TAG);
    }

    default String getDescription() {
        return getContent().get(DESCRIPTION_TAG);
    }
}
