package com.thehuginn.util;

import jakarta.ws.rs.WebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Arrays;
import java.util.Locale;

public class Helper {

    private Helper() {
    }

    public static void checkLocale(String locale) {
        if (locale == null || !Arrays.asList(Locale.getISOLanguages()).contains(locale)) {
            throw new WebApplicationException("Trying to create unknown locale", RestResponse.StatusCode.BAD_REQUEST);
        }
    }
}
