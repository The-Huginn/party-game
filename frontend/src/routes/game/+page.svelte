<script lang="ts">
	import { _, isLoading } from 'svelte-i18n';
	import type { PageData } from './$types';
	import { game_url, header_text } from '../../store';
	import { goto } from '$app/navigation';

	export let data: PageData;
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
		console.log(response);
		if (response.status == 201) {
			goto('/game/lobby');
			return { success: true };
		}
	}
	$header_text = 'Game Creation';
</script>

<svelte:head>
	<title>Game Creation</title>
	<meta name="description" content="Creation of a game session" />
</svelte:head>

<section>
	{#if $isLoading}
		<p>Loading</p>
	{:else}
		<h1>{$_('page.game.choose_name')}</h1>
	{/if}

	<!-- <a href="/lobby">click me</a> -->
	<div class="flex flex-col justify-center items-center w-full">
		<form
			class="w-full flex flex-col max-w-xs space-y-5"
			method="POST"
			on:submit|preventDefault={handleSubmit}
		>
			<div class="w-full form-control max-w-xs">
				<label for="gameId" class="label">
					<span class="label-text">{$_('page.game.game_name')}</span>
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
					{$_('page.game.game_name')}
				{/if}
			</button>
		</form>
	</div>
	<!-- on:click|preventDefault={() => window.location.href='/lobby'}  -->
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
