<script lang="ts">
	import { goto } from '$app/navigation';
	import { setCookie } from '$lib/common/cookies';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { onDestroy } from 'svelte';
	import { isLoading, locale } from 'svelte-i18n';
	import { game_url, header, task_url } from '../../store';
	import type { PageData } from './$types';
	import type { Category } from './Category';
	import CategoryTable from './CategoryTable.svelte';

	export let data: PageData;
	export let formSuccess: string = '';

	$: categories = data.categories;
	$: selected = data.selected;

	async function handleSubmit(event: SubmitEvent) {
		const formDatam = new FormData(this);

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
				formSuccess = 'page.task.category.missing_category';
			}
		}
	}

	$: subscription = locale.subscribe(async (newLocale) => {
		if (newLocale == null) {
			return;
		}

		const allCategories = await fetch(`${task_url}/task-mode/category`, {
			method: 'GET',
			credentials: 'include'
		});

		const selectedCategories = await fetch(`${task_url}/task-mode/category/selected`, {
			method: 'GET',
			credentials: 'include'
		});

		categories = (await allCategories.json()) as Category[];
		selected = (await selectedCategories.json());
	});
	$: onDestroy(subscription);

	$header = { text: 'page.task.category.title', append: '' };
</script>

<div class="flex flex-col w-full items-center justify-center space-y-5">
	<div
		class="grid relative w-4/5 lg:w-2/5 gap-4 p-4 mb-4 bg-info shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
	>
		<span class="font-bold text-3xl">{$_(`page.task.category.table_name`)}</span>
		<CategoryTable bind:categories {selected} />
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
