<script lang="ts">
	import Alert from '$lib/components/Alert.svelte';
	import { _, isLoading } from 'svelte-i18n';
	import { slide } from 'svelte/transition';
	import { header_text } from '../../store';
	import CategoryTable from './CategoryTable.svelte';
	import type { PageData } from './$types';

	export let data: PageData;
	export let formSuccess: boolean = true;

	let { categories } = data;

	export const ssr = false;
	$header_text = 'page.task.category.title';
</script>
<div class="flex flex-col w-full items-center justify-center space-y-5">
	<div class="parent">
		<span>{$_(`page.task.category.table_name`)}</span>
		<CategoryTable bind:categories={categories} />
	</div>
		<form method="GET" action="/">
			<button class="btn btn-primary transition duration-300">
				{#if $isLoading}
					<p>Loading</p>
				{:else}
					{$_('page.task.category.confirm')}
				{/if}
			</button>
		</form>
</div>

<style lang="css">
	.parent {
		position: relative;
		width: 40%;
		display: grid;
		gap: 1rem;
		margin-bottom: 1rem;
		padding: 1rem;
		background-color: hsl(220 20% 24%);
		box-shadow: 0 0 10px hsl(0 0% 0% / 10%);
		border: 1px solid hsl(220 20% 28%);
		border-radius: 1rem;
	}

	span {
		font-weight: 700;
		font-size: 2rem;
	}
</style>
