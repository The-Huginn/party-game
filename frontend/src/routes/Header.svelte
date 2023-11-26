<script lang="ts">
	import { _ } from '$lib/i18n/i18n-init';
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
			<svg version="1.0" xmlns="http://www.w3.org/2000/svg"
				width="512.000000pt" height="512.000000pt" viewBox="0 0 512.000000 512.000000"
				preserveAspectRatio="xMidYMid meet" class="object-contain w-12 h-12 fill-accent">
				<title>Home</title>
				<g transform="translate(0.000000,512.000000) scale(0.100000,-0.100000)" stroke="none">
					<path
						d="M2457 4816 c-20 -7 -52 -25 -70 -39 -87 -66 -2326 -2042 -2355 -2078
			-42 -53 -42 -83 0 -109 31 -19 52 -20 421 -20 l387 0 0 -1058 0 -1058 25 -51
			c16 -32 41 -61 67 -79 l41 -29 474 -3 473 -3 0 706 c0 569 3 712 13 738 17 39
			87 104 124 114 16 5 246 8 513 8 l485 0 47 -28 c31 -18 57 -44 75 -75 l28 -47
			3 -708 3 -708 469 3 468 3 43 30 c23 17 53 50 66 74 l23 44 0 1064 0 1063 383
			0 c344 0 386 2 417 18 43 21 49 50 21 95 -28 46 -2349 2086 -2408 2117 -57 30
			-176 38 -236 16z" />
				</g>
			</svg>
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
					<span>
						{$_(`${$header.text}`).trim()}
						{@html $header.append != '' ? ' ' + $header.append.trim() : ''}
					</span>
				{/if}
			</h1>
		{/key}
	</div>
	<div class="w-16 h-16 hover:shadow-lg">
		<a
			class="flex items-center justify-center w-full h-full"
			href="https://github.com/The-Huginn/party-game"
		>
		<!-- Github Logo inline SVG -->
		<svg class="object-contain w-12 h-12 fill-accent" xmlns="http://www.w3.org/2000/svg" viewBox="-3 -3 30 30">
			<title>GitHub</title>
			<path
				fill-rule="evenodd"
				clip-rule="evenodd"
				d="M12 2C6.47715 2 2 6.47715 2 12C2 17.5229 6.47715 22 12 22C17.5229 22 22 17.5229 22 12C22 6.47715 17.5229 2 12 2ZM0 12C0 5.3726 5.3726 0 12 0C18.6274 0 24 5.3726 24 12C24 18.6274 18.6274 24 12 24C5.3726 24 0 18.6274 0 12Z"
			/>
			<path
				fill-rule="evenodd"
				clip-rule="evenodd"
				d="M9.59162 22.7357C9.49492 22.6109 9.49492 21.4986 9.59162 19.399C8.55572 19.4347 7.90122 19.3628 7.62812 19.1833C7.21852 18.9139 6.80842 18.0833 6.44457 17.4979C6.08072 16.9125 5.27312 16.8199 4.94702 16.6891C4.62091 16.5582 4.53905 16.0247 5.84562 16.4282C7.15222 16.8316 7.21592 17.9303 7.62812 18.1872C8.04032 18.4441 9.02572 18.3317 9.47242 18.1259C9.91907 17.9201 9.88622 17.1538 9.96587 16.8503C10.0666 16.5669 9.71162 16.5041 9.70382 16.5018C9.26777 16.5018 6.97697 16.0036 6.34772 13.7852C5.71852 11.5669 6.52907 10.117 6.96147 9.49369C7.24972 9.07814 7.22422 8.19254 6.88497 6.83679C8.11677 6.67939 9.06732 7.06709 9.73672 7.99999C9.73737 8.00534 10.6143 7.47854 12.0001 7.47854C13.386 7.47854 13.8777 7.90764 14.2571 7.99999C14.6365 8.09234 14.94 6.36699 17.2834 6.83679C16.7942 7.79839 16.3844 8.99999 16.6972 9.49369C17.0099 9.98739 18.2372 11.5573 17.4833 13.7852C16.9807 15.2706 15.9927 16.1761 14.5192 16.5018C14.3502 16.5557 14.2658 16.6427 14.2658 16.7627C14.2658 16.9427 14.4942 16.9624 14.8233 17.8058C15.0426 18.368 15.0585 19.9739 14.8708 22.6234C14.3953 22.7445 14.0254 22.8257 13.7611 22.8673C13.2924 22.9409 12.7835 22.9822 12.2834 22.9982C11.7834 23.0141 11.6098 23.0123 10.9185 22.948C10.4577 22.9051 10.0154 22.8343 9.59162 22.7357Z"
			/>
		</svg>
		</a>
	</div>
	<div class="h-16 flex items-center justify-center hover:shadow-lg z-20">
		<div class="dropdown dropdown-hover dropdown-end">
			<!-- svelte-ignore a11y-no-noninteractive-tabindex -->
			<label for="" tabindex="0" class="cursor-pointer">
				<!-- Settings Logo inline SVG -->
				<svg class="object-contain w-12 h-12 stroke-accent button" xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 48 48" width="96px" height="96px">
					<title>Settings</title>
					<circle cx="24" cy="24" r="6.5" fill="none" stroke-width="3" />
					<path fill="none" stroke-linecap="round" stroke-linejoin="round" stroke-width="3"
						d="M30.8,35.7c1.2-0.7,2.8-0.8,4.1-0.2l2.7,1.2c1.9-2,3.4-4.5,4.2-7.3l-2.4-1.7c-1.2-0.8-1.8-2.2-1.8-3.6	s0.7-2.8,1.8-3.6l2.4-1.7c-0.8-2.8-2.3-5.2-4.2-7.3l-2.7,1.2c-1.3,0.6-2.8,0.5-4.1-0.2s-2.1-2-2.2-3.4L28.2,6	c-1.4-0.3-2.8-0.5-4.2-0.5S21.1,5.7,19.8,6" />
					<path fill="none" stroke-linecap="round" stroke-linejoin="round" stroke-width="3"
						d="M17.3,12.3c-1.2,0.7-2.8,0.8-4.1,0.2l-2.7-1.2c-1.9,2-3.4,4.5-4.2,7.3l2.4,1.7c1.2,0.8,1.8,2.2,1.8,3.6	s-0.7,2.8-1.8,3.6l-2.4,1.7c0.8,2.8,2.3,5.2,4.2,7.3l2.7-1.2c1.3-0.6,2.8-0.5,4.1,0.2s2.1,2,2.2,3.4l0.3,2.9	c1.4,0.3,2.8,0.5,4.2,0.5s2.9-0.2,4.2-0.5" />
				</svg>
			</label>
			<ul class="dropdown-content z-[1] menu p-2 shadow bg-base-100 rounded-box w-40 space-y-3">
				<li>
					{#if $isLoading}
						<span class="loading loading-spinner text-info" />
					{:else}
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
					{/if}
				</li>
				<li>
					{#if $isLoading}
						<span class="loading loading-spinner text-info" />
					{:else}
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
					{/if}
				</li>
			</ul>
		</div>
	</div>
</header>
