<!-- <script lang="ts">
	import { goto, invalidateAll } from '$app/navigation';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { isLoading } from 'svelte-i18n';
	import { game_url, header_text } from '../../store';
	import type { Task } from '../../routes/game/task/Task';
	import { onMount } from 'svelte';

	let formSuccess: string = '';
	let rawTask;
	let task: Task;
	export let handleSubmit;

	onMount(async () => {
		const response = await fetch(`${game_url}/game/next`, {
			method: 'PUT',
			credentials: 'include'
		});

		const data = (await response.json()).data;
		rawTask = data;
		task = data as Task;
	});
	// TODO
	$header_text = 'page.task.task.retrieve-from-backend';
</script>

{#if rawTask}
	<div class="flex flex-col w-full items-center justify-center space-y-5">
		<div
			class="grid relative w-2/5 gap-4 p-4 mb-4 bg-gray-700 shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
		>
			<h1 id={task.task}>{rawTask[task.task]}</h1>
		</div>
		<!-- <form on:submit|preventDefault={handleSubmit}>
			<button class="btn btn-primary transition duration-300">
				{#if $isLoading}
					<span class="loading loading-spinner text-info" />
				{:else}
					{$_('page.game.task.next')}
				{/if}
			</button>
			{#if formSuccess != ''}
				<Alert message={formSuccess} />
			{/if}
		</form> -->
	</div>
{/if} -->
