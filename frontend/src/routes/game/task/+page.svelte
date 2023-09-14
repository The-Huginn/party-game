<script lang="ts">
	import { page } from '$app/stores';
	import { setCookie } from '$lib/common/cookies';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { onDestroy } from 'svelte';
	import { isLoading, locale } from 'svelte-i18n';
	import { game_url, header } from '../../../store';
	import type { PageData } from './$types';
	import type { Task, Timer } from './Task';
	import TimerComponent from './TimerComponent.svelte';
	import PairTable from './PairTable.svelte';
	import shot from '$lib/images/shot.svg';

	export let data: PageData;
	let formSuccess: string = '';

	$: rawTask = data.data;
	$: task = rawTask as Task;
	$: timer = data.data.timer as Timer;
	$: initialLoad = data.initialLoad;

	async function submitHandler(event: SubmitEvent) {
		const response = await fetch(`${game_url}/mode/next`, {
			method: 'PUT',
			credentials: 'include'
		});

		data = {
			url: $page.url.toString(),
			data: (await response.json()).data,
			initialLoad: true
		};
	}

	$: subscription = locale.subscribe(async (newLocale) => {
		if (newLocale == null) {
			return;
		}

		if (typeof window !== 'undefined') {
			setCookie('locale', newLocale.substring(0, 2));
			const response = await fetch(`${game_url}/mode/current`, {
				method: 'GET',
				credentials: 'include'
			});

			rawTask = (await response.json()).data as Task;
		}
	});

	$: onDestroy(subscription);
	$: {
		$header.text = 'page.game.task.' + task.task_type.toLowerCase();
		if (task.task_type == 'SINGLE') {
			$header.append = task.player;
		} else {
			$header.append = '';
		}
	}
</script>

<div class="flex flex-col w-full items-center justify-center space-y-5">
	{#if task.pairs}
		<PairTable pairs={task.pairs} />
	{/if}
	<div
		class="grid relative w-3/5 gap-4 p-4 mb-4 bg-info shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
	>
	{#if task.price.enabled}
	<div class="absolute flex flex-row w-full justify-end whitespace-nowrap -mt-6 mx-4">
		<div class="flex flex-row float-right items-center justify-center bg-warning rounded-2xl p-2">
			<p class="text-xl font-bold">{$_('page.game.task.price')}</p>
			{#each Array(task.price.price) as _}
				<img class="object-contain w-8 h-8" src={shot} alt="price" />
			{/each}
		</div>
	</div>
	{/if}
		<h1 class="pt-4">
			<span class="font-bold text-4xl">
				{rawTask[task.task]}
			</span>
		</h1>
		{#if !initialLoad && timer}
			<progress value={timer.duration / timer.initialDuration} />
		{/if}
	</div>
	{#key timer}
		<TimerComponent {timer} />
	{/key}
	<form on:submit|preventDefault={submitHandler}>
		<button class="btn btn-primary transition duration-300 min-h-16 text-3xl">
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
