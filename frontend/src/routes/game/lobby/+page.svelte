<script lang="ts">
	import { fade, slide } from 'svelte/transition';
	import type { PageData } from './$types';
	import { game_url, header_text } from '../../../store';
	import { _, isLoading } from 'svelte-i18n';

	export let data: PageData;
	let { players } = data;

	console.log(players);

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

		class Player {
			id: number;
			name: string;

			public constructor(init?:Partial<Player>) {
				Object.assign(this, init);
			}
		};

		if (response.status == 200) {
			const player = (await response.json()) as Player;
			players = [...players, new Player(player)]
			return {
				success: true,
			};
		}
	}

	export const ssr = false;
	$header_text = 'page.game.lobby.title';
</script>

<div class="parent" transition:fade>
	<span>Lobby</span>
	{#each players as player}
		<div class="child" transition:slide|local>{player.name}</div>
	{/each}
	<form
		class="w-full flex flex-col max-w-xs space-y-5"
		method="POST"
		on:submit|preventDefault={handleSubmit}
	>
		<div class="child w-full form-control max-w-xs" transition:slide|local>
			<input
				type="text"
				name="new-player"
				class="input input-primary input-bordered w-full max-w-xs"
			/>
		</div>
		<button class="btn btn-primary w-full max-w-xs transition duration-300">
			{#if $isLoading}
				Loading
			{:else}
				{$_('page.game.lobby.add_player')}
			{/if}
		</button>
	</form>
</div>

<style lang="css">
	.parent {
		position: relative;
		width: 400px;
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

	.child {
		padding: 1rem;
		background-color: hsl(220 20% 28%);
		box-shadow: 0 0 10px hsl(0 0% 0% / 10%);
		border: 1px solid hsl(220 20% 32%);
		border-radius: 1rem;
	}
</style>
