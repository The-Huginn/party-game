import { readable, writable } from "svelte/store";

export const game_url = 'http://localhost:8080'
export const task_url = 'http://localhost:8081'
export const header_text = writable('Party Game');