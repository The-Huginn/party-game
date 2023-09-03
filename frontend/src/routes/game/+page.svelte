<script lang="ts">
	import { goto } from '$app/navigation';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { isLoading } from 'svelte-i18n';
	import { game_url, header_text } from '../../store';
	import type { PageData } from './$types';
	import { onMount } from 'svelte';
	import { getCookie } from '$lib/common/cookies';
	import Modal from './Modal.svelte';

	$: formSuccess = '';

	export let data: PageData;
	let { gameIdFallback } = data;
	let gameId: string;
	let gameIdCookie: string;

	onMount(() => {
		gameIdCookie = getCookie('gameId');
		gameId = gameIdCookie ?? gameIdFallback;
	});

	async function handleSubmit(event: SubmitEvent) {
		formSuccess = '';

		const formDatam = new FormData(this);
		gameId = formDatam.get('gameId')?.toString() ?? "";

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

<section class="flex flex-col justify-center items-center w-3/5 text-xl">
	{#if $isLoading}
		<span class="loading loading-spinner text-info" />
	{:else}
		<h1 class="w-full m-10 text-4xl font-bold">{$_('page.game.create.choose_name')}</h1>
	{/if}
	{#if gameIdCookie != ""}
		<Modal cookie={gameIdCookie}/>
	{/if}
	<div class="flex flex-col w-full gap-4 p-4 mb-4 bg-gray-700 shadow-lg border-1 border-solid border-gray-800 rounded-2xl justify-center items-center">
		<form class="w-4/5 flex flex-col space-y-5" on:submit|preventDefault={handleSubmit}>
			<div class="w-full form-control">
				<label for="gameId" class="label">
					<span class="font-bold text-3xl w-full">{$_('page.game.create.game_name')}</span>
				</label>
				<input
					type="text"
					name="gameId"
					value={gameId}
					class="input input-primary input-bordered w-full min-h-16 text-3xl"
				/>
			</div>
			<button class="btn btn-primary w-full min-h-16 text-xl transition duration-300">
				{#if $isLoading}
					<span class="loading loading-spinner text-info" />
				{:else}
					{$_('page.game.create.game_name')}
				{/if}
			</button>
			{#if formSuccess == 'page.game.create.missing_value'}
				<Alert message={formSuccess} />
			{:else if formSuccess == 'page.game.create.conflict'}
				<Modal cookie={gameId} type='conflict'/>
			{/if}
		</form>
	</div>
</section>
