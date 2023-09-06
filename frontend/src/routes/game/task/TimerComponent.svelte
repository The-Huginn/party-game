<script lang="ts">
	import { _ } from '$lib/i18n/i18n-init';
	import beep_mp4 from '$lib/sounds/beep.mp4';
	import { onDestroy } from 'svelte';
	import { isLoading } from 'svelte-i18n';
	import { Sound } from 'svelte-sound';
	import { quintOut } from 'svelte/easing';
	import { crossfade } from 'svelte/transition';
	import type { Timer } from './Task';

	export let timer: Timer;
	const beepSound = new Sound(beep_mp4);
	let initialLoad = true satisfies boolean;

	let timerInterval: ReturnType<typeof setTimeout>;
	let timerTimeout: ReturnType<typeof setTimeout>;

	function startTimer() {
		// remove the button
		timer.autostart = true;
		timerInterval = setInterval(() => {
			if (timer.duration > 0) timer.duration--;
		}, 10);
	}

	$: if (initialLoad) {
		initialLoad = false;
		if (timer) {
			timer.duration *= 100;
			timer.initialDuration = timer.duration;
			if (timer.autostart) {
				timerTimeout = setTimeout(() => {
					startTimer();
				}, (timer.delay ?? 0) * 1000); // this should be always defined
			}
		}
	}

	function beep() {
		beepSound.play();
		return '';
	}

	const [send, receive] = crossfade({
		duration: 1500,
		easing: quintOut
	});

	onDestroy(() => {
		clearInterval(timerInterval);
		clearTimeout(timerTimeout);
	});
</script>

{#if timer}
	<div class="w-full flex flex-col items-center justify-center p-8">
		{#if timer.duration > 0 && timer.autostart}
			<div
				out:send={{ key: 'a' }}
				in:receive={{ key: 'a' }}
				class="absolute radial-progress text-primary"
				style="--value:{Math.round((timer.duration / timer.initialDuration) * 100)};"
			>
				{Math.round(timer.duration / 10) / 10}s
			</div>
		{:else if Math.round(timer.duration) == 0}
			{beep()}
		{/if}
		{#if !timer.autostart}
			<form
				class="absolute"
				out:send={{ key: 'a' }}
				in:receive={{ key: 'a' }}
				on:submit|preventDefault={startTimer}
			>
				<button class="btn btn-secondary transition duration-300 min-h-16 text-3xl">
					{#if $isLoading}
						<span class="loading loading-spinner text-info" />
					{:else}
						{$_('page.game.task.start_timer')}
					{/if}
				</button>
			</form>
		{/if}
	</div>
{/if}
