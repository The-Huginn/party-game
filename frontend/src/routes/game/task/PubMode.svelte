<script lang="ts">
	import { page } from '$app/stores';
	import { _, isLoading, locale } from 'svelte-i18n';
	import type { PageData } from '../../$types';
	import { game_url, header } from '../../../store';
	import { onDestroy } from 'svelte';
	import Loading from '$lib/components/Loading.svelte';

	export let data: PageData;

	$: rawTask = data;
	let nextCallback;

	function submitHandler(event: SubmitEvent) {
		nextCallback = nextTask(event);
	}

	async function nextTask(event: SubmitEvent) {
		const response = await fetch(`${game_url}/mode/next`, {
			method: 'PUT',
			credentials: 'include'
		});

		rawTask = (await response.json()).data;
        return;
	}

	$: subscription = locale.subscribe(async (newLocale) => {
		if (newLocale == null) {
			return;
		}

		const response = await fetch(`${game_url}/mode/current`, {
			method: 'GET',
			credentials: 'include'
		});

		rawTask = (await response.json()).data;
	});

	$: onDestroy(subscription);
	$: $header.text = 'page.game.task.title';
</script>

<div class="flex flex-col w-full items-center justify-center space-y-5">
	<div
		class="grid relative w-4/5 lg:w-2/5 gap-4 p-4 mb-4 bg-info shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
	>
		<h1 class="pt-4">
			<span class="font-bold text-4xl">
				{#await nextCallback}
					<Loading />
				{:then}
					{@html rawTask[rawTask.task]}
				{/await}
			</span>
		</h1>
	</div>
	<form on:submit|preventDefault={submitHandler}>
		<button class="btn btn-primary transition duration-300 min-h-16 text-3xl">
			{#if $isLoading}
				<span class="loading loading-spinner text-info" />
			{:else}
				{$_('page.game.task.next')}
			{/if}
		</button>
	</form>
</div>
