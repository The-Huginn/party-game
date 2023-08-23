package com.thehuginn.services.exposed;

import com.thehuginn.GameSession;
import com.thehuginn.category.Category;
import com.thehuginn.category.LocaleCategory;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Map;

@Path("/task-mode")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class GameCreationService {

    @GET
    public Uni<GameSession> getGame(@RestCookie String gameId) {
        return GameSession.findById(gameId);
    }

    @GET
    @Path("/category")
    @WithTransaction
    public Uni<List<Category.CategoryDto>> getCategories() {
        return Category.findAll().project(Category.CategoryDto.class).list();
    }

    @GET
    @Path("/category/translation/{id}/{locale}")
    public Uni<Map<String, String>> getTranslation(@RestPath long id, @RestPath @DefaultValue("en") String locale) {
        return LocaleCategory.translation(id, locale);
    }

    @PUT
    @WithTransaction
    @Path("/category/{categoryId}")
    public Uni<Boolean> addCategory(@RestCookie String gameId, @RestPath Long categoryId) {
        return findGameSession(gameId)
                .chain(gameSession -> gameSession.addCategory(categoryId));
    }

    @DELETE
    @WithTransaction
    @Path("/category/{categoryId}")
    public Uni<Boolean> removeCategory(@RestCookie String gameId, @RestPath Long categoryId) {
        return findGameSession(gameId)
                .chain(gameSession -> gameSession.removeCategory(categoryId));
    }

    private Uni<GameSession> findGameSession(String gameId) {
        return GameSession.<GameSession>findById(gameId)
                .onItem().ifNull().failWith(new WebApplicationException("Unable to find game session"));
    }
}
