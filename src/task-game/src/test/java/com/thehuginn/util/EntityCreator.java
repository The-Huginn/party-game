package com.thehuginn.util;

import com.thehuginn.task.Category;
import com.thehuginn.task.Task;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EntityCreator {

    public static Task createTask(String task) {

        return new Task.Builder(task)
                .type(Task.Type.ALL)
                .repeat(Task.Repeat.PER_PLAYER)
                .frequency((short) 3)
                .price(new Task.Price(true, 1))
                .build();
    }

    public static Category createCategory(long... taskIds) {
        Category category = new Category();
        category.name = "name";
        category.description = "description";
        category.tasks = Arrays.stream(taskIds)
                .mapToObj(value -> {
                    Task task = createTask("this is a task");
                    task.id = value;

                    return task;
                })
                .collect(Collectors.toSet());
        return category;
    }
}
