//package com.thehuginn.token.unresolved;
//
//import com.thehuginn.task.ResolutionContext;
//import com.thehuginn.token.resolved.ResolvedToken;
//import com.thehuginn.token.resolved.TextResolvedToken;
//import io.smallrye.mutiny.Uni;
//import jakarta.persistence.Entity;
//
//@Entity
//public class TextUnresolvedToken extends AbstractUnresolvedToken {
//
//    public TextUnresolvedToken() {}
//
//    public TextUnresolvedToken(String key) {
//        super(key);
//    }
//
//    @Override
//    public Uni<? extends ResolvedToken> resolve(ResolutionContext context) {
//        return TextResolvedToken.getInstance(getKey(), context);
//    }
//}
