import { getCookie } from "$lib/common/cookies";
import { game_url } from "../../store";
import type { PageLoad } from "./lobby/$types";

export const prerender = true;
export const ssr = false;

export const load: PageLoad = async ({ fetch }) => {
    const getGameId = await fetch(`${game_url}/game/random`)

    const gameIdFallback = getGameId.text()

    return {
        gameId: gameIdFallback
    };
}