<script lang="ts">
	import { _, isLoading } from 'svelte-i18n';
	import { slide } from 'svelte/transition';
	import { game_url } from '../../../store';
	import type Player from './Player';

	export let players: Player[] = [];

	async function handleSubmit(event) {
		const formDatam = new FormData(this);
		const id: number = formDatam.get('id');

		const response = await fetch(`${game_url}/player`, {
			method: 'DELETE',
			headers: {
				'Content-type': 'application/json'
			},
			credentials: 'include',
			body: id as unknown as BodyInit
		});

		const reader = response.body?.getReader();
		let { value: chunk, done: readerDone } = await reader.read();
		const success = new TextDecoder().decode(chunk);
		if (response.status == 200 && success === 'true') {
			players = players.filter((player) => player.id != id);
		}
	}
</script>

<div class="overflow-x-auto">
	<table class="table table-auto">
		<tbody class="table-auto">
			{#each players as player}
				<tr class="items-center" transition:slide|local>
					<th class="w-full">
						<div class="w-full flex items-center justify-center space-x-3">
							<div class="font-bold">{player.name}</div>
						</div>
					</th>
					<th class="float-right nowrap">
						<form class="w-full flex" on:submit|preventDefault={handleSubmit}>
							<input type="hidden" name="id" hidden value={player.id} />
							<button class="btn btn-error text-white btn-xs float-right">
								{#if $isLoading}
									<span class="loading loading-spinner text-info" />
								{:else}
									{$_('page.game.lobby.delete_player')}
								{/if}
							</button>
						</form>
					</th>
				</tr>
			{/each}
		</tbody>
	</table>
</div>
