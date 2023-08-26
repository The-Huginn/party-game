<script lang="ts">
	import Alert from '$lib/components/Alert.svelte';
	import { _, isLoading } from 'svelte-i18n';
	import { fade, slide } from 'svelte/transition';
	import { game_url, header_text } from '../../../store';
	import type { PageData } from './$types';
	import LobbyTable from './LobbyTable.svelte';
	import Player from './Player';

	export let data: PageData;
	export let formSuccess: boolean = true;

	let { players } = data;

	async function handleSubmit(event) {
		const formDatam = new FormData(this);
		const newPlayer = formDatam.get('new-player');

		const response = await fetch(`${game_url}/player`, {
			method: 'POST',
			headers: {
				'Content-type': 'application/json'
			},
			credentials: 'include',
			body: JSON.stringify({ name: newPlayer })
		});

		if (response.status == 200) {
			const player = (await response.json()) as Player;
			players = [...players, new Player(player)];
			event.target.reset();
			formSuccess = true;
		} else {
			formSuccess = false;
		}
	}

	export const ssr = false;
	$header_text = 'page.game.lobby.title';
</script>

<div class="parent" transition:fade>
	<span>Lobby</span>
	<LobbyTable bind:players={players} />
	<form
		class="w-full flex flex-col space-y-5"
		on:submit|preventDefault={handleSubmit}
	>
		<div class="w-full form-control" transition:slide|local>
			<input
				type="text"
				name="new-player"
				class="input input-primary input-bordered w-full"
			/>
		</div>
		<button class="btn btn-primary w-full transition duration-300">
			{#if $isLoading}
				<span class="loading loading-spinner text-info" />
			{:else}
				{$_('page.game.lobby.add_player')}
			{/if}
		</button>
		{#if !formSuccess}
			<Alert message="page.game.lobby.submit_error" />
		{/if}
	</form>
</div>

<style lang="css">
	.parent {
		position: relative;
		width: 40%;
		display: grid;
		gap: 1rem;
		margin-bottom: 1rem;
		padding: 1rem;
		background-color: hsl(220 20% 24%);
		box-shadow: 0 0 10px hsl(0 0% 0% / 10%);
		border: 1px solid hsl(220 20% 28%);
		border-radius: 1rem;
		overflow: hidden;
	}

	span {
		font-weight: 700;
		font-size: 2rem;
	}
</style>
