<script lang="ts">
	import { goto } from '$app/navigation';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { isLoading } from 'svelte-i18n';
	import { game_url, header_text } from '../../store';
	import type { PageData } from './$types';
	import { onMount } from 'svelte';
	import { getCookie } from '$lib/common/cookies';

	export let formSuccess: string = '';

	export let data: PageData;
	let { gameIdFallback } = data;
	let gameId: string;

	onMount(() => {
    	gameId = getCookie('gameId') ?? gameIdFallback;
	})

	async function handleSubmit(event) {
		const formDatam = new FormData(this);
		const gameId = formDatam.get('gameId');

		if (gameId?.toString().length == 0) {
			formSuccess = 'page.game.create.missing_value';
			return;
		}
		const response = await fetch(`${game_url}/game`, {
			method: 'POST',
			headers: {
				'Content-type': 'application/json'
			},
			credentials: 'include',
			body: gameId
		});
		
		if (response.status == 201) {
			goto('/game/lobby');
			formSuccess = '';
		} else if (response.status == 409) {
			formSuccess = 'page.game.create.conflict';
		}
	}
	
	$header_text = 'page.game.create.title';
</script>

<svelte:head>
	<title>Game Creation</title>
	<meta name="description" content="Creation of a game session" />
</svelte:head>

<section class="flex flex-col justify-center items-center w-3/5">
	{#if $isLoading}
		<span class="loading loading-spinner text-info" />
	{:else}
		<h1 class="w-full">{$_('page.game.create.choose_name')}</h1>
	{/if}

	<div class="flex flex-col justify-center items-center w-full">
		<form class="w-full flex flex-col max-w-xs space-y-5" on:submit|preventDefault={handleSubmit}>
			<div class="w-full form-control max-w-xs">
				<label for="gameId" class="label">
					<span class="label-text">{$_('page.game.create.game_name')}</span>
				</label>
				<input
					type="text"
					name="gameId"
					value={gameId}
					class="input input-primary input-bordered w-full max-w-xs"
				/>
			</div>
			<button class="btn btn-primary w-full max-w-xs transition duration-300">
				{#if $isLoading}
					<span class="loading loading-spinner text-info" />
				{:else}
					{$_('page.game.create.game_name')}
				{/if}
			</button>
			{#if formSuccess != ''}
				<Alert message={formSuccess} />
			{/if}
		</form>
	</div>
</section>
