<script lang="ts">
	import { browser } from '$app/environment';
	import { page } from '$app/stores';
	import { GAME_ID, LOCALE } from '$lib/common/contants';
	import { getCookie } from '$lib/common/cookies';
	import shot from '$lib/images/shot.svg';

    const params = new URLSearchParams({
        gameId: getCookie(GAME_ID) ?? '',
        locale: getCookie(LOCALE) ?? 'en',
        })
    const shareCallbackUrl = $page.url.origin + "/share?" + params;
</script>

<svelte:head>
    {#if browser}
        <script src="https://unpkg.com/@bitjson/qr-code@1.0.2/dist/qr-code.js"></script>
    {/if}
</svelte:head>

<qr-code
	id="qr1"
	contents={shareCallbackUrl}
	module-color="#1c7d43"
	position-ring-color="#13532d"
	position-center-color="#70c559"
	mask-x-to-y-ratio="1.2"
	style="
        width: 200px;
        height: 200px;
        margin: 2em auto;
        background-color: #fff;
        "
>
	<img class="object-contain w-12 h-12" src={shot} slot="icon" alt="Share" />
</qr-code>
