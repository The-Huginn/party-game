//package com.thehuginn.token.resolved;
//
//import com.thehuginn.task.ResolutionContext;
//import io.quarkus.panache.common.Parameters;
//import io.smallrye.mutiny.Uni;
//import jakarta.persistence.Entity;
//
//import java.util.Map;
//
//@Entity
//public class TextResolvedToken extends AbstractResolvedToken {
//
//    String key;
//
//    TextResolvedToken() {}
//
//    private TextResolvedToken(String key) {
//        this.key = key;
//    }
//
//    public static Uni<TextResolvedToken> getInstance(String key, ResolutionContext context) {
//        return TextResolvedToken.<TextResolvedToken>find("from TextResolvedToken where key = :key", Parameters.with("key", key))
//                .firstResult()
//                .onItem()
//                .ifNull()
//                .switchTo(new TextResolvedToken(key).persist());
//    }
//
//    @Override
//    public ResolvedResult resolve(ResolutionContext context, ResolvedResult result) {
//        return result.appendData(Map.entry(key, LocaleText.byLocale(key, context.getLocale())));
//    }
//}
