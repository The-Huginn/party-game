<script lang="ts">
	import { isLoading } from 'svelte-i18n';
	import { _ } from '$lib/i18n/i18n-init';
	import { header_text } from '../../store';
	import type { PageData } from './$types';
	import CategoryTable from './CategoryTable.svelte';

	export let data: PageData;

	let { categories } = data;

	$header_text = 'page.task.category.title';
</script>

<div class="flex flex-col w-full items-center justify-center space-y-5">
	<div
		class="grid relative w-2/5 gap-4 p-4 mb-4 bg-gray-700 shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
	>
		<span class="font-bold font-4xl">{$_(`page.task.category.table_name`)}</span>
		<CategoryTable bind:categories />
	</div>
	<form method="GET" action="/">
		<button class="btn btn-primary transition duration-300">
			{#if $isLoading}
				<span class="loading loading-spinner text-info" />
			{:else}
				{$_('page.task.category.confirm')}
			{/if}
		</button>
	</form>
</div>
