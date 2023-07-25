package com.thehuginn.util;

import com.thehuginn.entities.Task;

import java.util.Arrays;

public class EntityCreator {

    public static Task createTask(long id) {
        Task task = new Task.Builder(Arrays.asList("drink responsibly", "<player_1>"))
                .id(id)
                .type(Task.Type.ALL)
                .repeat(Task.Repeat.PER_PLAYER)
                .frequency((short) 3)
                .price(new Task.Price(true, 1))
                .timer(new Task.Timer(true, 15))
                .build();

        return task;
    }
}
