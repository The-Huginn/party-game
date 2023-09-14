<script lang="ts">
	import { _ } from '$lib/i18n/i18n-init';
	import welcome_fallback from '$lib/images/svelte-welcome.png';
	import welcome from '$lib/images/svelte-welcome.webp';
	import { isLoading } from 'svelte-i18n';
	import { header } from '../store';

	$header = {text: 'page.disclaimer.title', append: ''};
</script>

<svelte:head>
	<title>Disclaimer</title>
	<meta name="description" content="Disclaimer for accessing this page" />
</svelte:head>

<section class="flex flex-col justify-center items-center w-4/5 m-5">
	<h1 class="w-full">
		<span class="block relative w-full h-full welcome">
			<picture>
				<source srcset={welcome} type="image/webp" />
				<img class="block absolute w-full h-full top-0" src={welcome_fallback} alt="Welcome" />
			</picture>
		</span>
	</h1>

	{#if $isLoading}
		<span class="loading loading-spinner text-info" />
	{:else}
		<h1>{@html $_('page.disclaimer.text')}</h1>
	{/if}

	<form method="GET" action="/game">
		<button class="btn btn-primary transition duration-300">
			{#if $isLoading}
				<span class="loading loading-spinner text-info" />
			{:else}
				{$_('page.disclaimer.accept_btn')}
			{/if}
		</button>
	</form>
</section>

<style>
	.welcome {
		padding: 0 0 calc(100% * 495 / 2048) 0;
	}
</style>
