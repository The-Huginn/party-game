import type { Mode } from "$lib/common/mode";
import { game_url } from "../../../store";
import type { PageLoad } from "./$types";

export const load: PageLoad = async ({ fetch }) => {
    const response = await fetch(`${game_url}/mode/current`, {
        method: 'GET',
        credentials: 'include'
    });
    
    const reply = await response.json();
    return { 
        data: reply.data,
        type: reply.type satisfies Mode,
    };
}