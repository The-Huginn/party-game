<script lang="ts">
	import { _, isLoading } from "svelte-i18n";
	import { slide } from "svelte/transition";

	export class Player {
		id: number;
		name: string;

		public constructor(init?: Partial<Player>) {
			Object.assign(this, init);
		}
	}
	export let players: Player[] = [];
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
						<button class="btn btn-error text-white btn-xs float-right">
							{#if $isLoading}
								<span class="loading loading-spinner text-info" />
							{:else}
								{$_('page.game.lobby.delete_player')}
							{/if}
						</button>
					</th>
				</tr>
			{/each}
		</tbody>
	</table>
</div>
