package com.thehuginn.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
    @NamedQuery(name = "LocaleText.byKey", query = "from LocaleText where task.id = :task_id and locale = :locale and key = :key"),
//        @NamedQuery(name = "LocaleText.getKeys", query = "select distinct(t.key) from LocaleText t where task.id = :task_id")
    @NamedQuery(name = "LocaleText.getKeys", query = "select t from LocaleText t where task.id = :task_id")
})
@IdClass(LocaleText.LocaleTextPK.class)
public class LocaleText extends PanacheEntityBase {

    public static class LocaleTextPK {
        public long task;
        public String key;
        public String locale;

        public LocaleTextPK() {}

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof LocaleTextPK)) {
                return false;
            } else {
                LocaleTextPK pk = (LocaleTextPK) obj;
                return task == pk.task && key.equals(pk.key) && locale.equals(pk.locale);
            }
        }

        @Override
        public int hashCode() {
            return Long.hashCode(task) + key.hashCode() + locale.hashCode();
        }
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "task_id")
    @JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
    @JsonIdentityReference(alwaysAsId=true)
    public Task task;

    @Id
    public String key;

    @Id
    public String locale = "en";

    public String content = "<missing_value>";

    public LocaleText() {}

    public LocaleText(String key) {
        this.key = key;
    }

    public static Uni<LocaleText> byKey(Long taskId, String locale, String key) {
        return LocaleText.<LocaleText>find("#LocaleText.byKey", Parameters.with("task_id", taskId).and("locale", locale).and("key", key))
                .firstResult()
                .onFailure().recoverWithItem(() -> new LocaleText(key));
    }

    public static Uni<List<String>> getKeys(Long taskId) {
        return LocaleText.<LocaleText>find("#LocaleText.getKeys", Parameters.with("task_id", taskId))
                .list()
                .map(localeTexts -> localeTexts.stream()
                        .map(localeText -> localeText.key)
                        .distinct()
                        .toList()
                )
                .onFailure()
                .recoverWithItem(new ArrayList<>());
    }
}
