<script lang="ts">
	import github from '$lib/images/github.svg';
	import home from '$lib/images/home-button.svg';
	import { isLoading } from 'svelte-i18n';
	import { _ } from '$lib/i18n/i18n-init';
	import * as eases from 'svelte/easing';
	import { slide, type EasingFunction } from 'svelte/transition';
	import { header_text } from '../store';

	export let duration: number = 300;
	export let easing: EasingFunction = eases.quartInOut;
</script>

<header class="flex justify-between z-10">
	<div class="w-16 h-16 hover:shadow-lg">
		<a class="flex items-center justify-center w-full h-full" href="/">
			<img class="object-contain w-12 h-12" src={home} alt="Home" />
		</a>
	</div>
	<div class="w-16 h-16 flex-1 px-10">
		{#key $header_text}
			<h1
				class="flex relative justify-center items-center w-full h-full inset-0 text-6xl mt-2"
				in:slide={{ duration, delay: duration, easing }}
				out:slide={{ duration, easing, axis: 'x' }}
			>
				{#if $isLoading}
					<span class="loading loading-spinner text-info" />
				{:else}
					{$_(`${$header_text}`)}
				{/if}
			</h1>
		{/key}
	</div>

	<div class="w-16 h-16 hover:shadow-lg">
		<a
			class="flex items-center justify-center w-full h-full"
			href="https://github.com/The-Huginn/party-game"
		>
			<img class="object-contain w-12 h-12" src={github} alt="GitHub" />
		</a>
	</div>
</header>
