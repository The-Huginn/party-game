<script lang="ts">
	import { fade, slide } from 'svelte/transition';
	import type { PageData } from './$types';
	import { header_text } from '../../../store';

	export let data: PageData;
	const {players} = data

	console.log(players)

	function add() {
		// children = [...children, children.length + 1];
	}

	export const ssr = false
	$header_text = 'Lobby';
</script>

	<div class="parent" transition:fade>
		<span>Lobby</span>
		{#each players as player}
			<div class="child" transition:slide|local>Child</div>
		{/each}
		<button on:click={add}>Add</button>
	</div>

<style lang="css">
	.parent {
		position: relative;
		width: 400px;
		display: grid;
		gap: 1rem;
		margin-bottom: 1rem;
		padding: 1rem;
		background-color: hsl(220 20% 24%);
		box-shadow: 0 0 10px hsl(0 0% 0% / 10%);
		border: 1px solid hsl(220 20% 28%);
		border-radius: 1rem;
		overflow: hidden;
	}

	span {
		font-weight: 700;
		font-size: 2rem;
	}

	.child {
		padding: 1rem;
		background-color: hsl(220 20% 28%);
		box-shadow: 0 0 10px hsl(0 0% 0% / 10%);
		border: 1px solid hsl(220 20% 32%);
		border-radius: 1rem;
	}
</style>
