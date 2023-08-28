<script lang="ts">
	import { invalidateAll } from '$app/navigation';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import beep_mp4 from '$lib/sounds/beep.mp4';
	import { isLoading } from 'svelte-i18n';
	import { Sound } from 'svelte-sound';
	import type { PageData } from './$types';
	import type { Task } from './Task';

	export let data: PageData;
	let formSuccess: string = '';

	const taskData = data.data as Task;
	$: rawTask = data.data;
	$: task = taskData;

	let timerDuration = -1;
	const beepSound = new Sound(beep_mp4);
	if (taskData.timer) {
		timerDuration = taskData.timer.duration;
		setInterval(() => {
			if (timer > 0) timer--;
		}, 10);
	}
	
	$: timer = timerDuration * 100;
</script>

<div class="flex flex-col w-full items-center justify-center space-y-5">
	<div
		class="grid relative w-2/5 gap-4 p-4 mb-4 bg-gray-700 shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
	>
		<h1 id={task.task}>{rawTask[task.task]}</h1>
		<progress value={timer / timerDuration / 100} />
	</div>
	{#if timer > 0}
		<div class="radial-progress text-primary" style="--value:{timer / timerDuration};">
			{timer / 100}%
		</div>
	{:else if timer == 0}
		{beepSound.play()}
	{/if}
	<form on:submit|preventDefault={invalidateAll}>
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
