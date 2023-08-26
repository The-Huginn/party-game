<script lang="ts">
	import github from '$lib/images/github.svg';
	import home from '$lib/images/home-button.svg';
	import { _, isLoading } from 'svelte-i18n';
	import { fade, slide, type EasingFunction } from 'svelte/transition';
	import * as eases from 'svelte/easing';
	import { header_text } from '../store';

	export let duration: number = 300;
	export let easing: EasingFunction = eases.quartInOut;
</script>

<header>
	<div class="corner hover:shadow-lg">
		<a href="/">
			<img src={home} alt="Home" />
		</a>
	</div>
	<div class="corner flex-1 px-10">
		{#key $header_text}
			<h1
				in:slide={{ duration, delay: duration, easing }}
				out:slide={{ duration, easing, axis: 'x' }}
			>
				{#if $isLoading}
					Loading
				{:else}
					{$_(`${$header_text}`)}
				{/if}
			</h1>
		{/key}
	</div>

	<div class="corner hover:shadow-lg">
		<a href="https://github.com/The-Huginn/party-game">
			<img src={github} alt="GitHub" />
		</a>
	</div>
</header>

<style>
	header {
		display: flex;
		justify-content: space-between;
		z-index: 1;
	}

	.corner {
		width: 4em;
		height: 4em;
	}

	.corner a {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 100%;
		height: 100%;
	}

	.corner img {
		width: 3em;
		height: 3em;
		object-fit: contain;
	}

	h1 {
		display: flex;
		position: relative;
		justify-content: center;
		align-items: center;
		width: 100%;
		height: 100%;
		inset: 0;
	}
</style>
