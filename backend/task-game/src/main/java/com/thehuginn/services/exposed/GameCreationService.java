package com.thehuginn.services.exposed;

import com.thehuginn.GameSession;
import com.thehuginn.category.Category;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.token.translation.CategoryText;
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
import java.util.Set;
import java.util.stream.Collectors;

@Path("/task-mode")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class GameCreationService {

    @GET
    @Path("/category")
    @WithTransaction
    public Uni<List<CategoryText.CategoryDto>> getCategories(@RestCookie @DefaultValue("en") String locale) {
        Uni<List<Uni<CategoryText.CategoryDto>>> localeCategories = Category.<Category> listAll().map(
                categories -> categories.stream()
                        .map(category -> category.categoryText.resolve(ResolutionContext.locale(locale)))
                        .toList());
        //noinspection unchecked
        return localeCategories.chain(unis -> Uni.combine().all().<List<CategoryText.CategoryDto>> unis(unis)
                .usingConcurrencyOf(1)
                .combinedWith(objects -> (List<CategoryText.CategoryDto>) objects));
    }

    @GET
    @Path("/category/selected")
    @WithTransaction
    public Uni<Set<CategoryText.CategoryDto>> getSelectedCategories(@RestCookie String gameId) {
        return findGameSession(gameId).map(gameSession -> gameSession.categories.stream()
                .map(CategoryText.CategoryDto::new)
                .collect(Collectors.toSet()));
    }

    @GET
    @Path("/category/translation/{id}/{locale}")
    public Uni<CategoryText.CategoryDto> getTranslation(@RestPath long id, @RestPath @DefaultValue("en") String locale) {
        return Category.<Category> findById(id)
                .flatMap(category -> category.categoryText.resolve(ResolutionContext.locale(locale)));
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
        return GameSession.<GameSession> findById(gameId)
                .onItem().ifNull().failWith(new WebApplicationException("Unable to find game session"));
    }
}
