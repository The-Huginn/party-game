package com.thehuginn.common.game;

import com.thehuginn.common.game.category.AbstractCategory;
import com.thehuginn.common.game.translation.CategoryText;
import com.thehuginn.common.game.translation.LocaleCategoryText;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RunOnVertxContext
public class CategoryTextTest {

    @BeforeEach
    @AfterEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> CategoryText.deleteAll());
        asserter.execute(() -> AbstractCategory.deleteAll());
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testPersistingCategory(UniAsserter asserter) {
        AbstractCategory category = new AbstractCategory();
        CategoryText categoryText = new CategoryText();
        categoryText.category = category;
        categoryText.name = "test";
        categoryText.description = "test description";
        category.categoryText = categoryText;
        asserter.execute(() -> category.persistAndFlush());
        asserter.assertThat(() -> CategoryText.<CategoryText> findAll().firstResult(), categoryText1 -> {
            Assertions.assertEquals("test", categoryText1.name);
            Assertions.assertEquals("test description", categoryText1.description);
            Assertions.assertEquals("en", categoryText1.locale);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testTranslatingCategory(UniAsserter asserter) {
        AbstractCategory category = new AbstractCategory();
        CategoryText categoryText = new CategoryText();
        categoryText.category = category;
        categoryText.name = "test";
        categoryText.description = "test description";
        category.categoryText = categoryText;
        LocaleCategoryText localeCategoryText = new LocaleCategoryText(categoryText, "sk", "Slovensky test", "Slovensky popis");
        asserter.execute(() -> category.persistAndFlush());
        asserter.execute(() -> localeCategoryText.persistAndFlush());
        asserter.assertThat(() -> LocaleCategoryText.<LocaleCategoryText> findAll().firstResult(), localeTaskText -> {
            Assertions.assertEquals("Slovensky test", localeCategoryText.name);
            Assertions.assertEquals("Slovensky popis", localeCategoryText.description);
            Assertions.assertEquals("sk", localeCategoryText.locale);
        });

        ResolutionContext resolutionContext = ResolutionContext.locale("sk");
        asserter.assertThat(() -> CategoryText.<CategoryText> findAll().firstResult()
                .chain(categoryText1 -> categoryText1.translate(resolutionContext)), categoryDto -> {
                    categoryDto.description = "Slovensky popis";
                    categoryDto.name = "Slovensky test";
                });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
