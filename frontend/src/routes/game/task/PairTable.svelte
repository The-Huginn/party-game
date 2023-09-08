<script lang="ts">
	import type { TeamPair } from './Task';

	export let pairs: TeamPair[];
</script>

<div class="grid relative w-2/5 gap-4 p-4 mb-4 bg-secondary shadow-lg rounded-2xl">
	<table class="min-w-full text-center text-sm font-light">
		<tbody class="table-auto">
			{#each pairs as pair}
				<tr class="whitespace-nowrap flex items-center justify-center">
					<th class="px-6 py-4 w-full">
						<div class="w-full flex items-center space-x-3">
							<p class="w-full font-bold text-2xl text-right">{pair.first}</p>
						</div>
					</th>
                    <th class="px-6 py-4 w-full">
						<div class="w-full flex items-center justify-center space-x-3">
							<div class="font-bold text-2xl">&</div>
						</div>
					</th>
                    <th class="px-6 py-4 w-full">
						<div class="w-full flex float-left space-x-3">
							<div class="font-bold text-2xl">{pair.second}</div>
						</div>
					</th>
				</tr>
			{/each}
		</tbody>
	</table>
</div>
