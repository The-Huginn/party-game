import { task_url } from "../../store";
import type { PageLoad } from "./$types";
import type { Category } from "./Category";

export const ssr = false;

export const load: PageLoad = async ({ fetch }) => {
    const allCategories = await fetch(`${task_url}/task-mode/category`, {
        method: 'GET',
        credentials: 'include'
    });

    const selectedCategories = await fetch(`${task_url}/task-mode/category/selected`, {
        method: 'GET',
        credentials: 'include'
    });

    return {
        categories: await allCategories.json() as Category[],
        selected: await selectedCategories.json(),
    };
}