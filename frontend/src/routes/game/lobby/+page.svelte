<script lang="ts">
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { isLoading } from 'svelte-i18n';
	import { slide } from 'svelte/transition';
	import { game_url, header } from '../../../store';
	import type { PageData } from './$types';
	import LobbyTable from './LobbyTable.svelte';
	import Player from './Player';
	import { goto } from '$app/navigation';

	export let data: PageData;
	export let formSuccess: boolean = true;
	$header = { text: 'page.game.lobby.title', append: '' };

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

	async function handleStart(event:SubmitEvent) {
		const response = await fetch(`${game_url}/mode/start`, {
			method: 'PUT',
			headers: {
				'Content-type': 'application/json'
			},
			credentials: 'include'
		});

		if (response.status == 200) {
			const success = (await response.json()) as Boolean;
			if (success == true) {
				goto('/game/task');
			} else {
				formSuccess = false;
			}
		}
	}
</script>

<div class="flex flex-col w-full items-center space-y-5">
	<div
		class="grid relative w-4/5 lg:w-2/5 gap-4 p-4 mb-4 bg-info shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
	>
		<span class="font-bold font-4xl text-3xl">{$_(`page.game.lobby.table_name`)}</span>
		<LobbyTable bind:players />
		<form class="w-full flex flex-col space-y-5" on:submit|preventDefault={handleSubmit}>
			<div class="w-full form-control" transition:slide|local>
				<input
					type="text"
					name="new-player"
					class="input input-primary input-bordered w-full min-h-16 text-3xl"
				/>
			</div>
			<button class="btn btn-primary w-full transition duration-300 min-h-16 text-xl">
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

	<form on:submit|preventDefault={handleStart}>
		<button class="btn btn-primary transition duration-300 min-h-16 text-xl">
			{#if $isLoading}
				<span class="loading loading-spinner text-info" />
			{:else}
				{$_('page.game.lobby.confirm')}
			{/if}
		</button>
	</form>
</div>
