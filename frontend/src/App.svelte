<script lang="ts">
	import { onMount } from 'svelte';
	import { themeChange } from 'theme-change';

	onMount(() => {
		themeChange(false);
	});

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
