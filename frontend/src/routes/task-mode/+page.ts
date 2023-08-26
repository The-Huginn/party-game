import { task_url } from "../../store";
import type { PageLoad } from "./$types";
import type { Category } from "./Category";

export const load: PageLoad = async ({ fetch }) => {
    const response = await fetch(`${task_url}/task-mode/category`, {
        method: 'GET',
        credentials: 'include'
    });
    return {
        categories: await response.json() as Category[],
    };
}