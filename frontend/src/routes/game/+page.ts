import { game_url } from "../../store";
import type { PageLoad } from "./$types";

export const load: PageLoad = async ({ fetch }) => {
    const getGameId = await fetch(`${game_url}/game/random`);

    const gameIdFallback = getGameId.text();

    return {
        gameIdFallback: gameIdFallback
    };
}