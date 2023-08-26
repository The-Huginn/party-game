// since there's no dynamic data here, we can prerender

import { task_url } from "../../store";
import type { PageLoad } from "./$types";
import type { Category } from "./Category";

// it so that it gets served as a static asset in production
export const prerender = true;
export const ssr = false;

export const load: PageLoad = async ({ fetch }) => {
    const response = await fetch(`${task_url}/task-mode/category`, {
        method: 'GET',
        credentials: 'include'
    });
    return {
        categories: await response.json() as Category[],
    };
}