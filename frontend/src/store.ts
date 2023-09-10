import { dev } from '$app/environment';
import { writable } from "svelte/store";

export const game_url = dev ? 'http://localhost:8080' : 'https://game.thehuginn.com/api/game'
export const task_url = dev ? 'http://localhost:8082' : 'https://game.thehuginn.com/api/task'
export const header = writable({
    text: 'Party Game',
    append: ''
});