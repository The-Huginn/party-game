package com.thehuginn.resolution;

import com.thehuginn.token.unresolved.PlayerUnresolvedToken;
import com.thehuginn.token.unresolved.TimerUnresolvedToken;
import com.thehuginn.token.unresolved.UnresolvedToken;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.annotation.Nonnull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TokenResolver {

    private static final Pattern tokenPattern = Pattern.compile("\\{.*?\\}");

    private TokenResolver() {
    }

    public static boolean isToken(@Nonnull String key) {
        return tokenPattern.matcher(key).find();
    }

    public static Tuple2<Class<? extends UnresolvedToken>, List<String>> resolveToken(@Nonnull String key) {
        String[] splitKey = key.substring(1, key.length() - 1).split("_");
        Class<? extends UnresolvedToken> tokenClass = switch (splitKey[0]) {
            case "player" -> PlayerUnresolvedToken.class;
            case "timer" -> TimerUnresolvedToken.class;
            default -> throw new IllegalStateException("Unexpected token detected with value: " + splitKey[0]);
        };

        return Tuple2.of(tokenClass, Stream.of(splitKey).skip(1).toList());
    }

    public static List<UnresolvedToken> translateTask(String task) {
        List<UnresolvedToken> tokens = new ArrayList<>();
        Matcher matcher = tokenPattern.matcher(task);
        while (matcher.find()) {
            String token = matcher.group();
            if (token.isBlank()) {
                throw new IllegalArgumentException("Token used in task templating should not be empty: " + task);
            }
            Tuple2<Class<? extends UnresolvedToken>, List<String>> resolvedToken = resolveToken(token.trim());
            try {
                tokens.add(resolvedToken.getItem1().getConstructor(String.class).newInstance(token));
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                Log.errorf(e, "Unable to instantiate following task [%s] and token [%s] into Token of [%s]",
                        task, token, resolvedToken.getItem1());
                throw new RuntimeException(e);
            }
        }

        return tokens;
    }
}
