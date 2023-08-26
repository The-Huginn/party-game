import { game_url } from "../../../store";
import type { PageLoad } from "./$types";
import type Player from "./Player";

export const prerender = true;

export const load: PageLoad = async ({ fetch }) => {
    const response = await fetch(`${game_url}/team`, {
        method: 'GET',
        credentials: 'include'
    });
    return {
        players: await response.json() as Player[],
    };
}