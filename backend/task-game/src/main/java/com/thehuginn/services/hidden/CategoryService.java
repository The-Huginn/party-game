package com.thehuginn.services.hidden;

import com.thehuginn.category.Category;
import com.thehuginn.task.Task;
import com.thehuginn.token.translation.CategoryText;
import com.thehuginn.token.translation.LocaleCategoryText;
import com.thehuginn.token.translation.TranslatableCategory;
import com.thehuginn.util.Helper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.DenyAll;
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

import java.util.Set;
import java.util.function.Function;

@DenyAll
@Path("/category")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CategoryService {

    @POST
    @WithTransaction
    public Uni<Category> createCategory(Category category) {
        return Task.findByIds(category.tasks)
                .<Category> chain(tasks -> {
                    if (category.tasks.size() != tasks.size()) {
                        throw new WebApplicationException("Unable to retrieve all tasks");
                    }
                    category.categoryText = new CategoryText(category, category.categoryText);
                    category.tasks.clear();
                    category.tasks.addAll(tasks);
                    return category.persist();
                })
                .call(category1 -> Task.addToCategory(category1.id, category1.tasks))
                .onFailure().invoke(Log::error);
    }

    @PUT
    @Path("/{id}")
    @WithTransaction
    public Uni<Category> updateCategory(@RestPath Long id, Category category) {
        return Category.findByIdFetch(id)
                .call(category1 -> Task.deleteFromCategory(category1.id, category1.tasks))
                .call(category1 -> Task.addToCategory(category1.id, category.tasks))
                .map(category1 -> {
                    category1.categoryText.name = category.categoryText.name != null ? category.categoryText.name
                            : category1.categoryText.name;
                    category1.categoryText.description = category.categoryText.description != null
                            ? category.categoryText.description
                            : category1.categoryText.description;
                    category1.tasks = category.tasks != null ? category.tasks : category1.tasks;

                    return category1;
                });
    }

    @DELETE
    @Path("/{id}")
    @WithTransaction
    public Uni<Boolean> deleteCategory(@RestPath long id) {
        return Category.getTasks(id)
                .call(tasks -> Task.deleteFromCategory(id, tasks))
                .chain(tasks -> Category.deleteById(id));
    }

    @GET
    @Path("/{id}")
    public Uni<Set<Task>> getTasks(@RestPath long id) {
        return Category.getTasks(id);
    }

    @POST
    @Path("/translation/{id}/{locale}")
    public Uni<CategoryText.CategoryDto> createTranslation(@RestPath Long id, @RestPath String locale,
            CategoryText.CategoryDto categoryDto) {
        Helper.checkLocale(locale);
        return Category.<Category> findById(id)
                .<LocaleCategoryText> chain(category -> {
                    LocaleCategoryText newLocale = new LocaleCategoryText(category.categoryText, locale, categoryDto.name,
                            categoryDto.description);
                    return newLocale.persist();
                })
                .map(localeCategoryText -> new CategoryText.CategoryDto(id, localeCategoryText.getName(),
                        localeCategoryText.getDescription()));
    }

    @PUT
    @Path("/translation/{id}/{locale}")
    public Uni<CategoryText.CategoryDto> updateTranslation(@RestPath long id, @RestPath String locale,
            CategoryText.CategoryDto categoryDto) {
        Helper.checkLocale(locale);
        Function<CategoryText, Uni<LocaleCategoryText>> translation = categoryText -> LocaleCategoryText
                .<LocaleCategoryText> findById(new LocaleCategoryText.LocaleCategoryTextPK(categoryText, locale))
                .onItem().ifNotNull().transformToUni(localeCategoryText -> {
                    localeCategoryText.name = categoryDto.name;
                    localeCategoryText.description = categoryDto.description;
                    return localeCategoryText.persistAndFlush();
                });
        Uni<? extends TranslatableCategory> translatableCategoryUni = Category.<Category> findById(id)
                .map(category -> category.categoryText)
                .onItem().ifNotNull().transformToUni(categoryText -> {
                    // our CategoryText is being changed
                    if (categoryText.getLocale().equals(locale)) {
                        categoryText.name = categoryDto.name != null ? categoryDto.name : categoryText.name;
                        categoryText.description = categoryDto.description != null ? categoryDto.description
                                : categoryText.description;
                        return categoryText.persistAndFlush();
                    }
                    // one of the translations is being changed
                    return translation.apply(categoryText);
                })
                .onFailure().invoke(Log::error);

        return translatableCategoryUni
                .map(translatableCategory -> new CategoryText.CategoryDto(id, translatableCategory.getName(),
                        translatableCategory.getDescription()));
    }
}
