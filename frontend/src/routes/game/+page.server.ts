// since there's no dynamic data here, we can prerender

import { game_url } from "../../store";
import type { PageServerLoad } from "./$types";
import type { Actions } from "@sveltejs/kit";
import { goto } from "$app/navigation";

// it so that it gets served as a static asset in production
export const prerender = true;

export const load: PageServerLoad = async ({ fetch, cookies }) => {
    const getGameId = await fetch(`${game_url}/game/random`)

    const gameId = cookies.get('gameId') ?? getGameId.text()

    console.log(gameId);

    return {
        gameId: gameId
    };
}