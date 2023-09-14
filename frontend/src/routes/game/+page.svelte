<script lang="ts">
	import { goto } from '$app/navigation';
	import { getCookie, setCookie } from '$lib/common/cookies';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { onMount } from 'svelte';
	import { isLoading } from 'svelte-i18n';
	import Modal from '../../lib/components/Modal.svelte';
	import { game_url, header } from '../../store';
	import type { PageData } from './$types';

	$: formSuccess = '';

	export let data: PageData;
	let { gameIdFallback } = data;
	let gameId: string;
	let cookie: string;

	onMount(() => {
		cookie = getCookie('gameId');
		gameId = cookie ?? gameIdFallback;
	});

	async function handleSubmit(event: SubmitEvent) {
		formSuccess = '';

		const formDatam = new FormData(this);
		gameId = formDatam.get('gameId')?.toString() ?? '';

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
			cookie = gameId;
			formSuccess = 'page.game.create.conflict';
		}
	}

	$header = { text: 'page.game.create.title', append: '' };

	let gameState: string = 'CREATED';

	async function onMountCallback() {
		const response = await fetch(`${game_url}/game?gameId=${cookie}`);

		if (response.status == 200) {
			gameState = (await response.json()).state;
		}
	}

	function yesCallback() {
		setCookie('gameId', cookie);
		switch (gameState) {
			case 'CREATED':
				goto('/game/lobby');
				break;
			case 'LOBBY':
			case 'READY':
				// TODO update based on game type
				// TODO add mode-selection route
				break;
			case 'ONGOING':
				break;
		}
	}

	function noCallback() {}
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
	{#if cookie != ''}
		<Modal {onMountCallback} {yesCallback} {noCallback} question="page.game.create.game_exists" />
	{/if}
	<div
		class="flex flex-col w-full gap-4 p-4 mb-4 bg-info shadow-lg border-1 border-solid border-gray-800 rounded-2xl justify-center items-center"
	>
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
				<Modal
					{onMountCallback}
					{yesCallback}
					{noCallback}
					question="page.game.create.game_conflicts"
				/>
			{/if}
		</form>
	</div>
</section>
