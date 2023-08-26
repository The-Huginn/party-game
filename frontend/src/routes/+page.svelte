<script lang="ts">
	import welcome_fallback from '$lib/images/svelte-welcome.png';
	import welcome from '$lib/images/svelte-welcome.webp';
	import { _, isLoading } from 'svelte-i18n';
	import type { PageData } from './$types';
	import Router, { location, push } from 'svelte-spa-router';
	import { header_text } from '../store';

	export let data: PageData;

	$header_text = 'page.disclaimer.title';
</script>

<svelte:head>
	<title>Disclaimer</title>
	<meta name="description" content="Disclaimer for accessing this page" />
</svelte:head>

<section>
	
	<h1>
		<span class="welcome">
			<picture>
				<source srcset={welcome} type="image/webp" />
				<img src={welcome_fallback} alt="Welcome" />
			</picture>
		</span>
	</h1>

	{#if $isLoading}
		<p>Loading</p>
	{:else}
		<h1>{$_('page.disclaimer.text')}</h1>
	{/if}

	<p>Current page {$location}</p>

	<form method="GET" action="/game">
		<button class="btn btn-primary transition duration-300">
			{#if $isLoading}
				<p>Loading</p>
			{:else}
				{$_('page.disclaimer.accept_btn')}
			{/if}
		</button>
	</form>

</section>

<style>
	section {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		flex: 0.6;
	}

	h1 {
		width: 100%;
	}

	.welcome {
		display: block;
		position: relative;
		width: 100%;
		height: 0;
		padding: 0 0 calc(100% * 495 / 2048) 0;
	}

	.welcome img {
		position: absolute;
		width: 100%;
		height: 100%;
		top: 0;
		display: block;
	}
</style>
