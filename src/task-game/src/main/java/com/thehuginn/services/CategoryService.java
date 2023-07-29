package com.thehuginn.services;

import com.thehuginn.entities.Category;
import com.thehuginn.entities.Task;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
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

import java.util.List;
import java.util.Set;

@Path("/category")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CategoryService {

    @GET
    @WithTransaction
    public Uni<List<Category.CategoryDto>> getCategories() {
        return Category.findAll().project(Category.CategoryDto.class).list();
    }

    @POST
    @WithTransaction
    public Uni<Category> createCategory(Category category) {
        return Task.findByIds(category.tasks)
                .onItem()
                .<Category>transformToUni(tasks -> {
                    if (category.tasks.size() != tasks.size()) {
                        throw new WebApplicationException("Unable to retrieve all tasks");
                    }
                    category.tasks = tasks;
                    return category.persist();
                })
                .onItem()
                .call(category1 -> Task.addToCategory(category1.id, category1.tasks))
                .onFailure()
                .invoke(throwable -> Log.error(throwable));
    }

    @PUT
    @Path("/{id}")
    @WithTransaction
    public Uni<Category> updateCategory(@RestPath Long id, Category category) {
        return Category.findByIdFetch(id)
                .onItem()
                .call(category1 -> Task.deleteFromCategory(category1.id, category1.tasks))
                .onItem()
                .call(category1 -> Task.addToCategory(category1.id, category.tasks))
                .onItem()
                .transform(category1 -> {
                    category1.name = category.name;
                    category1.description = category.description;
                    category1.tasks = category.tasks;

                    return category1;
                });
    }

    @DELETE
    @Path("/{id}")
    @WithTransaction
    public Uni<Boolean> deleteCategory(@RestPath long id) {
        return Category.getTasks(id)
                .onItem()
                .call(tasks -> Task.deleteFromCategory(id, tasks))
                .onItem()
                .transformToUni(tasks -> Category.deleteById(id));
    }

    @GET
    @Path("/{id}")
    public Uni<Set<Task>> getTasks(@RestPath long id) {
        return Category.getTasks(id);
    }
}
