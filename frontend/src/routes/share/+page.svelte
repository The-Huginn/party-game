<script>
	import { goto } from '$app/navigation';
    import { page } from '$app/stores'
	import { GAME_ID, LOCALE } from '$lib/common/contants';
	import { setCookie } from '$lib/common/cookies';

    const gameId = $page.url.searchParams.get('gameId');
    const locale = $page.url.searchParams.get('locale') ?? 'en';

    if (gameId == undefined) {
        goto('/')
    } else {
        setCookie(GAME_ID, gameId);
        setCookie(LOCALE, locale);
        goto('/game/task/');
    }
</script>