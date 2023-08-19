package com.thehuginn.services;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.TokenResolver;
import com.thehuginn.resolution.UnresolvedResult;
import com.thehuginn.task.Task;
import com.thehuginn.token.LocaleText;
import com.thehuginn.token.unresolved.AbstractUnresolvedToken;
import com.thehuginn.token.unresolved.UnresolvedToken;
import com.thehuginn.util.Helper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroupIterable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TaskService {

    @POST
    @WithTransaction
    public Uni<Task> createTask(@Valid Task task) {
        Helper.checkLocale(task.task.locale);
        Function<List<UnresolvedToken>, UniAndGroupIterable<UnresolvedToken>> findOrCreateTokens = resolvables -> {
            List<Uni<AbstractUnresolvedToken>> unis = resolvables.stream()
                    .map(resolvedTokenResolvable ->
                            AbstractUnresolvedToken.<AbstractUnresolvedToken>findById(((AbstractUnresolvedToken) resolvedTokenResolvable).getKey())
                                    .onItem().ifNull().switchTo(((AbstractUnresolvedToken) resolvedTokenResolvable).persist()))
                    .toList();
            return Uni.combine()
                    .all().<UnresolvedToken>unis(unis)
                    .usingConcurrencyOf(1);
        };
        return Uni.createFrom()
                .item(task)
                .call(task1 -> {
                    task1.task = new LocaleText(task1, task.task.locale, task.task.content);
                    if (task.tokens.isEmpty()) {
                        return Uni.createFrom().voidItem();
                    }
                    return findOrCreateTokens.apply(TokenResolver.translateTask(task1.task.content))
                            .combinedWith(objects -> task1.tokens = (List<UnresolvedToken>) objects);
                })
                .chain(task1 -> task1.persist());
    }

    @GET
    @Path("/{id}")
    public Uni<Task> getTask(@RestPath Long id) {
        return Task.findById(id);
    }

    @DELETE
    @Path("/{id}")
    @WithTransaction
    public Uni<Boolean> deleteTask(@RestPath Long id) {
        return Task.deleteById(id);
    }

    @PUT
    @Path("/{id}")
    @WithTransaction
    public Uni<Task> updateTask(@RestPath Long id, @Valid Task updatedTask) {
        return Task.<Task>findById(id)
                .<Task>chain(task -> {
                    if (updatedTask.task != null) {
                        if (updatedTask.task.content != null && !updatedTask.task.content.equals("<missing_value>")) {
                            task.task.content = updatedTask.task.content;
                            task.tokens.clear();
                            task.tokens.addAll(TokenResolver.translateTask(updatedTask.task.content));
                        }
                    }
                    task.type = updatedTask.type != null ? updatedTask.type : task.type;
                    task.repeat = updatedTask.repeat != null ? updatedTask.repeat : task.repeat;
                    task.frequency = updatedTask.frequency != null ? updatedTask.frequency : task.frequency;
                    task.price = updatedTask.price != null ? updatedTask.price : task.price;

                    return task.persist();
                })
                .onFailure().recoverWithNull();
    }

    @GET
    @Path("/{id}/{locale}")
    public Uni<Task> getLocale(@RestPath Long id, @RestPath String locale) {
        ResolutionContext resolutionContext = ResolutionContext.locale(locale);
        return Task.<Task>findById(id)
                .call(task -> {
                    UnresolvedResult unresolvedResult = task.task.resolve(resolutionContext);
                    return unresolvedResult.resolve()
                            .onItem()
                            .invoke(resolvedResult1 -> {
                                Map<String, Object> map = resolvedResult1.getData();
                                task.task = new LocaleText((String) map.get("locale"), (String) map.get(task.getKey()));
                            });
                });
    }

    @POST
    @Path("/{id}/{locale}")
    @WithTransaction
    public Uni<LocaleText> createLocale(@RestPath Long id, @RestPath String locale, String content) {
        Helper.checkLocale(locale);
        return Task.<Task>findById(id)
                .invoke(task -> preservesTokens(task.task, content))
                .chain(task -> {
                    LocaleText newLocale = new LocaleText();
                    newLocale.task = task;
                    newLocale.locale = locale;
                    newLocale.content = content;
                    return newLocale.persist();
                });
    }

    @PUT
    @Path("/{id}/{locale}")
    @WithTransaction
    public Uni<LocaleText> updateKey(@RestPath Long id, @RestPath String locale, String newContent) {
        return LocaleText.<LocaleText>findById(new LocaleText.LocaleTextPK(id, locale))
                .invoke(task -> preservesTokens(task, newContent))
                .onItem().ifNotNull().<LocaleText>transformToUni(localeText -> {
                    localeText.content = newContent;
                    return localeText.persist();
                })
                .onFailure().invoke(Log::error);
    }

    private void preservesTokens(LocaleText task, String content) {
        if (!TokenResolver.translateTask(task.content).equals(TokenResolver.translateTask(content))) {
            Log.warnf("Trying to create or update locale without preserving tokens in their respective order");
            throw new WebApplicationException("Trying to create or update locale without preserving tokens in their respective order", RestResponse.StatusCode.BAD_REQUEST);
        }
    }
}
