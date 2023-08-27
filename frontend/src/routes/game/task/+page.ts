import { game_url } from "../../../store";
import type { PageLoad } from "./$types";
import type { Task } from "./Task";

export const load: PageLoad = async ({ fetch, params }) => {
    const response = await fetch(`${game_url}/game/next`, {
        method: 'PUT',
        credentials: 'include'
    });

    const data = (await response.json()).data;
    return {
        rawTask: data,
        task: data as Task
    };
}