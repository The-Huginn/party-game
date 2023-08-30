<script lang="ts">
	import { invalidateAll } from '$app/navigation';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import beep_mp4 from '$lib/sounds/beep.mp4';
	import { isLoading } from 'svelte-i18n';
	import { Sound } from 'svelte-sound';
	import type { PageData } from './$types';
	import type { Task, Timer } from './Task';

	export let data: PageData;
	let formSuccess: string = '';

	const taskData = data.data as Task;
	$: rawTask = data.data;
	$: task = rawTask as Task;
	$: timer = data.data.timer as Timer;
	$: initialLoad = data.initialLoad;

	const beepSound = new Sound(beep_mp4);

	let timerInterval: ReturnType<typeof setTimeout>;
	let timerTimeout: ReturnType<typeof setTimeout>;
	$: if (initialLoad) {
		initialLoad = false;
		if (timer) {
			timer.duration *= 100;
			timer.initialDuration = timer.duration;
			if (timer.delay > 0) {
				timerTimeout = setTimeout(() => {
					timerInterval = setInterval(() => {
						if (timer.duration > 0) timer.duration--;
					}, 10);
				}, timer.delay * 1000);
			} else {
				timerInterval = setInterval(() => {
					if (timer.duration > 0) timer.duration--;
				}, 10);
			}
		}
	}

	async function submitHandler(event) {
		clearInterval(timerInterval);
		clearTimeout(timerTimeout);
		await invalidateAll();
	}

	function beep() {
		beepSound.play();
		return '';
	}
</script>

<div class="flex flex-col w-full items-center justify-center space-y-5">
	<div
		class="grid relative w-2/5 gap-4 p-4 mb-4 bg-gray-700 shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
	>
		<h1 id={task.task}>{rawTask[task.task]}</h1>
		{#if !initialLoad && timer}
			<progress value={timer.duration / timer.initialDuration} />
		{/if}
	</div>
	{#if timer}
		{#if timer.duration > 0}
			<div
				class="radial-progress text-primary"
				style="--value:{Math.round((timer.duration / timer.initialDuration) * 100)};"
			>
				{Math.round((timer.duration / timer.initialDuration) * 100)}%
			</div>
		{:else if Math.round(timer.duration) == 0}
			{beep()}
		{/if}
	{/if}
	<form on:submit|preventDefault={submitHandler}>
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
