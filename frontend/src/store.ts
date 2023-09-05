import { readable, writable } from "svelte/store";

export const game_url = 'http://localhost:8080'
export const task_url = 'http://localhost:8082'
export const header = writable({
    text: 'Party Game',
    append: ''
});