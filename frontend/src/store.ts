import { writable } from "svelte/store";

export const game_url = 'http://game-service'
export const task_url = 'http://task-game'
export const header = writable({
    text: 'Party Game',
    append: ''
});