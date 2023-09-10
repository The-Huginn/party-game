import { dev } from '$app/environment';
import { writable } from "svelte/store";

export const game_url = dev ? 'http://localhost:8080' : 'http://game-service'
export const task_url = dev ? 'http://localhost:8082' : 'http://task-game'
export const header = writable({
    text: 'Party Game',
    append: ''
});