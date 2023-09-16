<script lang="ts">
	import { _ } from '$lib/i18n/i18n-init';
	import github from '$lib/images/github.svg';
	import home from '$lib/images/home-button.svg';
	import settings from '$lib/images/settings.svg';
	import { isLoading, locale, locales } from 'svelte-i18n';
	import * as eases from 'svelte/easing';
	import { slide, type EasingFunction } from 'svelte/transition';
	import { header } from '../store';

	export let duration: number = 300;
	export let easing: EasingFunction = eases.quartInOut;
</script>

<header class="flex justify-between space-x-2 z-10 m-2">
	<div class="w-16 h-16 hover:shadow-lg">
		<a class="flex items-center justify-center w-full h-full" href="/">
			<img class="object-contain w-12 h-12" src={home} alt="Home" />
		</a>
	</div>
	<div class="w-16 h-16 flex-1 px-10">
		{#key $header.text}
			<h1
				class="flex relative justify-center items-center w-full h-full inset-0 text-4xl lg:text-6xl mt-2"
				in:slide={{ duration, delay: duration, easing }}
				out:slide={{ duration, easing, axis: 'x' }}
			>
				{#if $isLoading}
					<span class="loading loading-spinner text-info" />
				{:else}
					{$_(`${$header.text}`)} {$header.append != '' ? ' ' + $header.append : ''}
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
	<div class="h-16 flex items-center justify-center hover:shadow-lg z-20">
		<div class="dropdown dropdown-hover dropdown-end">
			<button><img class="object-contain w-12 h-12 button" src={settings} alt="Settings" /></button>
			<ul class="dropdown-content z-[1] menu p-2 shadow bg-base-100 rounded-box w-40 space-y-3">
				<li>
					{$_(`settings.theme`)}
					<select class="items-center justify-center select bg-opacity-30" data-choose-theme>
						<option disabled value=""> Pick a theme </option>
						<option value="">Default</option>
						<option value="light">Light</option>
						<option value="dark">Dark</option>
						<option value="cyberpunk">Cyberpunk</option>
						<option value="valentine">Valentine</option>
						<option value="lofi">Lofi</option>
						<option value="retro">Retro</option>
						<option value="dracula">Dracula</option>
						<option value="night">Night</option>
						<option value="halloween">Halloween</option>
					</select>
				</li>
				<li>
					{$_(`settings.language`)}
					<select class="items-center justify-center select bg-opacity-30" bind:value={$locale}>
						{#each $locales as value}
							{#if $locale == value}
								<option {value} selected>{value.substring(0, 2)}</option>
							{:else}
								<option {value}>{value.substring(0, 2)}</option>
							{/if}
						{/each}
					</select>
				</li>
			</ul>
		</div>
	</div>
</header>
