<script lang="ts">
	import { goto } from '$app/navigation';
	import Alert from '$lib/components/Alert.svelte';
	import { _, isLoading } from 'svelte-i18n';
	import { game_url, header_text } from '../../store';

	// export let data: PageData;
	export let formSuccess: boolean = true;
	export const ssr = false;

	async function handleSubmit(event) {
		const formDatam = new FormData(this);
		const gameId = formDatam.get('gameId');

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
			formSuccess = true;
		} else {
			formSuccess = false;
		}
	}
	$header_text = 'page.game.create.title';
</script>

<svelte:head>
	<title>Game Creation</title>
	<meta name="description" content="Creation of a game session" />
</svelte:head>

<section>
	{#if $isLoading}
		<p>Loading</p>
	{:else}
		<h1>{$_('page.game.create.choose_name')}</h1>
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
					class="input input-primary input-bordered w-full max-w-xs"
				/>
			</div>
			<button class="btn btn-primary w-full max-w-xs transition duration-300">
				{#if $isLoading}
					Loading
				{:else}
					{$_('page.game.create.game_name')}
				{/if}
			</button>
			{#if !formSuccess}
				<Alert message="page.game.create.submit_error" />
			{/if}
		</form>
	</div>
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
</style>
