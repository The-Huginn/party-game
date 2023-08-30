import { game_url } from "../../../store";
import type { PageLoad } from "./$types";

export const load: PageLoad = async ({ fetch }) => {
    const response = await fetch(`${game_url}/game/next`, {
        method: 'PUT',
        credentials: 'include'
    });
    
    return { 
        data: (await response.json()).data,
        initialLoad: true satisfies boolean
    };
}