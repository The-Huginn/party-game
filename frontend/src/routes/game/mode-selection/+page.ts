import { game_url } from "../../../store";
import type { PageLoad } from "./$types";

export const prerender = true;
export const ssr = false;

export const load: PageLoad = async ({ fetch }) => {
    const response = await fetch(`${game_url}/mode/exists`, {
        method: 'GET',
        credentials: 'include'
    });

    return {
        status: response.status,
    };
}