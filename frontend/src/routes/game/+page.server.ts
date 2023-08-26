import { game_url } from "../../store";
import type { PageServerLoad } from "./$types";

export const prerender = true;

export const load: PageServerLoad = async ({ fetch, cookies }) => {
    const getGameId = await fetch(`${game_url}/game/random`)

    const gameId = cookies.get('gameId') ?? getGameId.text()

    return {
        gameId: gameId
    };
}