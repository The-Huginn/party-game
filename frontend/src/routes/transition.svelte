<script lang="ts">
	import { slide, type EasingFunction } from 'svelte/transition';
	import * as eases from 'svelte/easing';

	export let key: string;
	export let duration: number = 300;
	export let axis = 'x';
	export let easing: EasingFunction = eases.quartInOut
</script>

{#key key}
	<div in:slide={{ duration, delay: duration, easing }} out:slide={{ duration, easing, axis: 'x' }}>
		<slot />
	</div>
{/key}

<style>
	div {
		display: flex;
		position: relative;
		justify-content: center;
		align-items: center;
		width: 100%;
		height: 100%;
		inset: 0;
	}
</style>
