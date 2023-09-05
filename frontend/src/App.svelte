<script lang="ts">
	import { setupI18n } from '$lib/i18n/i18n-init';
	import { onMount } from 'svelte';
	import { themeChange } from 'theme-change';

	onMount(() => {
		themeChange(false);
	});

	setupI18n();
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
