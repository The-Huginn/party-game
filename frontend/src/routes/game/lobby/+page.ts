// since there's no dynamic data here, we can prerender

import { game_url, task_url } from "../../../store";
import type { PageLoad } from "./$types";
import type { Player } from "./LobbyTable.svelte";

// it so that it gets served as a static asset in production
export const prerender = true;
export const ssr = false;

export const load: PageLoad = async ({ fetch }) => {
    const response = await fetch(`${game_url}/team`, {
        method: 'GET',
        credentials: 'include'
    });
    return {
        players: await response.json() as Player[],
    };
}