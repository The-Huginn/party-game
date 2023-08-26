<script lang="ts">
	import * as eases from 'svelte/easing';
	import { slide, type EasingFunction } from 'svelte/transition';

	export let key: string;
	export let duration: number = 300;
	export let easing: EasingFunction = eases.quartInOut;
</script>

{#key key}
	<div
		class="flex relative justify-center items-center w-full h-full inset-0"
		in:slide={{ duration, delay: duration, easing }}
		out:slide={{ duration, easing, axis: 'x' }}
	>
		<slot />
	</div>
{/key}
