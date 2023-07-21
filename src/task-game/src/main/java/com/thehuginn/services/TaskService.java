package com.thehuginn.services;

import com.thehuginn.entities.LocaleText;
import com.thehuginn.entities.Task;
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

import java.util.List;

@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TaskService {

    @POST
    @WithTransaction
    public Uni<Task> createTask(@Valid Task task) {
        return task.persist();
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
                .transform(task -> {
                    task.task = updatedTask.task;
                    task.type = updatedTask.type;
                    task.repeat = updatedTask.repeat;
                    task.frequency = updatedTask.frequency;
                    task.price = updatedTask.price;
                    task.timer = updatedTask.timer;

                    return task;
                })
                .onFailure()
                .recoverWithNull();
    }

    @GET
    @Path("/{taskId}/keys")
    public Uni<List<String>> getKeys(@RestPath Long taskId) {
        return LocaleText.getKeys(taskId);
    }

    @GET
    @Path("/{taskId}/{locale}/{key}")
    public Uni<LocaleText> getLocale(@RestPath Long taskId, @RestPath String locale, @RestPath String key) {
        return LocaleText.byKey(taskId, locale, key);
    }

    @POST
    @Path("/{taskId}")
    @WithTransaction
    public Uni<LocaleText> createKey(@RestPath Long taskId, LocaleText localeText) {
        return Task.<Task>findById(taskId)
                .map(task -> {
                    localeText.task = task;
                    return localeText;
                })
                .onItem().call(localeText1 -> localeText1.persist());

    }

    @PUT
    @Path("/{taskId}/{locale}/{key}")
    @WithTransaction
    public Uni<LocaleText> updateKey(@RestPath Long taskId, @RestPath String locale, @RestPath String key, String newContent) {
        return LocaleText.byKey(taskId, locale, key)
                .onItem()
                .transform(localeText -> {
                    localeText.content = newContent;

                    return localeText;
                });
    }
}
