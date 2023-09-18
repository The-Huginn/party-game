<script lang="ts">
	import { setCookie } from '$lib/common/cookies';
	import { onDestroy, onMount } from 'svelte';
	import { locale } from 'svelte-i18n';
	import { themeChange } from 'theme-change';

	onMount(() => {
		themeChange(false);
	});

	$: subscription = locale.subscribe(async (newLocale) => {
		if (newLocale == null) {
			return;
		}

		setCookie('locale', newLocale);
	});

	$: onDestroy(subscription);
	
</script>

<svelte:head>
	<title>Disclaimer</title>
	<meta name="description" content="Disclaimer for accessing this page" />
</svelte:head>

<style global>
	@tailwind base;
	@tailwind components;
	@tailwind utilities;
</style>
