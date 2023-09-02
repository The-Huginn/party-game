<script lang="ts">
	import { getCookie } from '$lib/common/cookies';
	import { onMount } from 'svelte';
	import { _ } from 'svelte-i18n';
	import { game_url } from '../../store';
	import { goto } from '$app/navigation';

	$: gameExists = false;
	let gameState: string;
	onMount(async () => {
		const response = await fetch(`${game_url}/game?gameId=${getCookie('gameId')}`);

		if (response.status == 200) {
			gameState = (await response.json()).state;
			gameExists = true;
		}
	});

	function handleSubmit(event: SubmitEvent) {
        gameExists = false;
		if (event.submitter?.id == 'continue') {
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
			gameExists = false;
		}
	}
	window.addEventListener('keydown', keyPress);
</script>

{#if true}
	<input type="checkbox" id="modal" class="modal-toggle" checked />
	<div class="modal">
		<div class="modal-box">
			<p>
				{$_('page.game.create.game_exists')}
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