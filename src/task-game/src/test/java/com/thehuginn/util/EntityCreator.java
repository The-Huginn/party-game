package com.thehuginn.util;

import com.thehuginn.entities.Category;
import com.thehuginn.entities.Task;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityCreator {

    public static Task createTask() {
        Task task = new Task.Builder(Arrays.asList("drink responsibly", "<player_1>"))
                .type(Task.Type.ALL)
                .repeat(Task.Repeat.PER_PLAYER)
                .frequency((short) 3)
                .price(new Task.Price(true, 1))
                .timer(new Task.Timer(true, 15))
                .build();

        return task;
    }

    public static Category createCategory(long... taskIds) {
        Category category = new Category();
        category.name = "name";
        category.description = "description";
        Set<Task> tasks = Arrays.stream(taskIds)
                .mapToObj(value -> {
                    Task task = createTask();
                    task.id = value;

                    return task;
                })
                .collect(Collectors.toSet());
        category.tasks = tasks;
        return category;
    }
}
