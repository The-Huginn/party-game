package com.thehuginn.services;

import com.thehuginn.task.Task;
import com.thehuginn.util.TokenResolver;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestPath;

@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TaskService {

    @POST
    @WithTransaction
    public Uni<Task> createTask(@Valid Task task) {
        return Uni.createFrom()
                .item(task)
                .invoke(task1 -> task1.tokens = TokenResolver.translateTask(task1.task.getKey()))
                .onItem()
                .transformToUni(task1 -> task1.persist());
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
                .onItem()
                .<Task>transformToUni(task -> {
                    if (updatedTask.task != null) {
                        task.task = updatedTask.task;
                        task.tokens.clear();
                        task.tokens.addAll(TokenResolver.translateTask(updatedTask.task.getKey()));
                    }
                    task.type = updatedTask.type != null ? updatedTask.type : task.type;
                    task.repeat = updatedTask.repeat != null ? updatedTask.repeat : task.repeat;
                    task.frequency = updatedTask.frequency != null ? updatedTask.frequency : task.frequency;
                    task.price = updatedTask.price != null ? updatedTask.price : task.price;

                    return task.persist();
                })
                .onFailure()
                .recoverWithNull();
    }

//    @GET
//    @Path("/{taskId}/keys")
//    public Uni<List<String>> getKeys(@RestPath Long taskId) {
//        return Task.<Task>findById(taskId)
//                .onItem()
//                .transform(task -> task.tokens.stream()
//                        .map(token -> token.key)
//                        .toList());
//    }

//    @GET
//    @Path("/{key}/{locale}/{key}")
//    public Uni<LocaleText> getLocale(@RestPath String key, @RestPath String locale) {
//        return LocaleText.byLocale(key, locale);
//    }
//
//    @POST
//    @Path("/{key}/{locale}")
//    @WithTransaction
//    public Uni<LocaleText> createLocale(@RestPath String key, @RestPath String locale, String content) {
//        return Task.TokenImpl.<Task.TokenImpl>find("key = :key", Parameters.with("key", key))
//                .firstResult()
//                .onItem()
//                .transformToUni(token -> {
//                    LocaleText localeText = new LocaleText();
//                    localeText.token = token;
//                    localeText.locale = locale;
//                    localeText.content = content;
//                    return localeText.persist();
//                });
//    }
//
//    @PUT
//    @Path("/{key}/{locale}/{key}")
//    @WithTransaction
//    public Uni<LocaleText> updateKey(@RestPath String key, @RestPath String locale, String newContent) {
//        return LocaleText.byLocale(key, locale)
//                .onItem()
//                .transformToUni(localeText -> {
//                    localeText.content = newContent;
//                    return localeText.persist();
//                });
//    }
}
