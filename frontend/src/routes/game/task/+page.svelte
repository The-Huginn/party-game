<script lang="ts">
	import { invalidateAll } from '$app/navigation';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { isLoading } from 'svelte-i18n';
	import type { PageData } from './$types';
	import type { Task } from '../../../routes/game/task/Task';

	export let data: PageData;
	let formSuccess: string = '';

	$: rawTask = data.data;
	$: task = rawTask as Task;

	let unique = {};

	async function handleSubmit(event) {
		invalidateAll();
	}
</script>

<div class="flex flex-col w-full items-center justify-center space-y-5">
	<div
		class="grid relative w-2/5 gap-4 p-4 mb-4 bg-gray-700 shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
	>
		<h1 id={task.task}>{rawTask[task.task]}</h1>
	</div>
	<form on:submit|preventDefault={handleSubmit}>
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
	</form>
</div>
