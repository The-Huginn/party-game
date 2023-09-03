<script lang="ts">
	import { goto } from '$app/navigation';
	import { setCookie } from '$lib/common/cookies';
	import { onMount } from 'svelte';
	import { _, isLoading } from 'svelte-i18n';
	import { game_url } from '../../store';

	export let cookie: string;
    type modalType = 'continue' | 'conflict';
    export let type: modalType = 'continue';
	$: modalShow = true;
	let gameState: string = "CREATED";

	onMount(async () => {
		const response = await fetch(`${game_url}/game?gameId=${cookie}`);

		if (response.status == 200) {
			gameState = (await response.json()).state;
		}
	});

	function handleSubmit(event: SubmitEvent) {
		modalShow = false;
		if (event.submitter?.id == 'continue') {
			setCookie('gameId', cookie);
			switch (gameState) {
				case 'CREATED':
					goto('/game/lobby');
					break;
				case 'LOBBY':
				case 'READY':
					// TODO update based on game type
					// TODO add mode-selection route
					break;
				case 'ONGOING':
					break;
			}
		}
	}

	function keyPress(ev: KeyboardEvent) {
		if (ev.key == 'Escape') {
			modalShow = false;
		}
	}
	window.addEventListener('keydown', keyPress);
</script>

{#if modalShow}
	<input type="checkbox" id="modal" class="modal-toggle" checked />
	<div class="modal">
		<div class="modal-box">
			<p>
                {#if $isLoading}
		            <span class="loading loading-spinner text-info" />
                {:else}
                    {#if type == 'continue'}
                        {$_('page.game.create.game_exists')}
                    {:else}
                        {$_('page.game.create.game_conflicts')}
                    {/if}
                {/if}
			</p>
			<form on:submit|preventDefault={handleSubmit}>
				<div class="modal-action">
					<button class="btn btn-primary" id="continue">yes</button>
					<button class="btn" id="recreate">no</button>
				</div>
			</form>
		</div>
	</div>
{/if}
