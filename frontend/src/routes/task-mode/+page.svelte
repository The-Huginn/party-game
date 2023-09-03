<script lang="ts">
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { isLoading } from 'svelte-i18n';
	import { game_url, header_text } from '../../store';
	import type { PageData } from './$types';
	import CategoryTable from './CategoryTable.svelte';
	import { goto } from '$app/navigation';

	export let data: PageData;
	export let formSuccess: string = '';

	let { categories } = data;
	let { selected } = data;

	async function handleSubmit(event) {
		const formDatam = new FormData(this);

		const response = await fetch(`${game_url}/game/start`, {
			method: 'PUT',
			headers: {
				'Content-type': 'application/json'
			},
			credentials: 'include'
		});

		if (response.status == 200) {
			const success = (await response.json()) as Boolean;
			if (success == true) {
				goto('/game/task')
			} else {
				formSuccess = 'page.task.category.missing_category';
			}
		}
	}

	$header_text = 'page.task.category.title';
</script>

<div class="flex flex-col w-full items-center justify-center space-y-5">
	<div
		class="grid relative w-2/5 gap-4 p-4 mb-4 bg-gray-700 shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
	>
		<span class="font-bold text-3xl">{$_(`page.task.category.table_name`)}</span>
		<CategoryTable bind:categories selected={selected}/>
	</div>
	<form class="w-full flex flex-col max-w-xs space-y-5" on:submit|preventDefault={handleSubmit}>
		<button class="btn btn-primary transition duration-300 min-h-16 text-xl">
			{#if $isLoading}
				<span class="loading loading-spinner text-info" />
			{:else}
				{$_('page.task.category.confirm')}
			{/if}
		</button>
		{#if formSuccess != ''}
			<Alert message={formSuccess} />
		{/if}
	</form>
</div>
