<script lang="ts">
	import tooltip from '$lib/images/tooltip.svg';
	import { slide } from 'svelte/transition';
	import { task_url } from '../../store';
	import type { Category } from './Category';

	export let categories: Category[] = [];
	export let selected: Category[] = [];

	async function handleSubmit(event) {
		const id: number = this.getAttribute('id');
		const method = this.checked == true ? 'PUT' : 'DELETE';

		const response = await fetch(`${task_url}/task-mode/category/${id}`, {
			method: method,
			headers: {
				'Content-type': 'application/json'
			},
			credentials: 'include'
		});

		const reader = response.body?.getReader();
		let { value: chunk, done: readerDone } = await reader.read();
		const success = new TextDecoder().decode(chunk);
		if (response.status == 200 && success === 'true') {
			// TODO
		}
	}
</script>

<div>
	<table class="table table-auto">
		<tbody class="table-auto">
			{#each categories as category}
				<tr class="items-center flex flex-col sm:flex-row" transition:slide|local>
					<th class="nowrap w-full">
						<form class="w-full flex">
							<div class="form-control space-x-10 flex flex-row items-center">
								<input type="hidden" name="id" hidden value={category.id} />
								{#if selected.filter((e) => e.id === category.id).length > 0}
									<input
										type="checkbox"
										id={category.id.toString()}
										on:click={handleSubmit}
										checked
										class="checkbox checkbox-warning"
									/>
								{:else}
									<input
										type="checkbox"
										id={category.id.toString()}
										on:click={handleSubmit}
										class="checkbox checkbox-warning"
									/>
								{/if}
								<label class="cursor-pointer label font-bold text-3xl" for={category.id.toString()}
									>{category.name}</label
								>
							</div>
						</form>
					</th>
					<th class="nowrap w-1/5">
						<div
							class="corner w-8 h-8 tooltip"
							role="tooltip"
							style="overflow: visible!important;"
							data-tip={category.description}
						>
							<button>
								<img class="object-contain" src={tooltip} alt={category.description} />
							</button>
						</div>
					</th>
				</tr>
			{/each}
		</tbody>
	</table>
</div>
