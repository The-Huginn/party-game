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

@Path("/category")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CategoryService {

    @GET
    public Uni<List<Category>> getCategories() {
        return Category.listAll();
    }

    @POST
    @WithTransaction
    public Uni<Category> createCategory(Category category) {
        return Uni.createFrom()
                .item(category.tasks)
                .onItem()
                .transformToUni(tasks -> Task.findByIds(category.tasks))
                .onItem()
                .transform(tasks -> {
                    if (category.tasks.size() != tasks.size()) {
                        throw new WebApplicationException("Unable to retrieve all tasks");
                    }
                    category.tasks = tasks;
                    return category;
                })
                .onItem()
                .<Category>transformToUni(category1 -> category1.persist())
                .onFailure()
                .invoke(throwable -> Log.error(throwable));
    }

    @PUT
    @WithTransaction
    public Uni<Category> updateCategory(Category category) {
        return Category.<Category>findById(category.id)
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
        return Category.deleteById(id);
    }

    @GET
    @Path("/{id}")
    public Uni<List<Task>> getTasks(@RestPath long id) {
        return Category.<Category>findById(id)
                .onItem()
                .transformToUni(Category::getTasks);
    }
}
