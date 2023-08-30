<script lang="ts">
	import { goto } from '$app/navigation';
	import { _ } from '$lib/i18n/i18n-init';
	import { isLoading } from 'svelte-i18n';
	import { game_url, header_text } from '../../../store';

	export let formSuccess: boolean = true;
	$header_text = 'page.game.mode-selection.title';

	async function handleSubmit(event) {
		const formDatam = new FormData(this);

		const response = await fetch(`${game_url}/game/create`, {
			method: 'POST',
			headers: {
				'Content-type': 'application/json'
			},
			credentials: 'include'
		});

		if (response.status == 200) {
			goto('/task-mode');
		} else {
			formSuccess = false;
		}
	}
</script>

<div class="flex flex-col w-2/5 items-center space-y-5">
	<form class="w-full flex flex-col space-y-5" on:submit|preventDefault={handleSubmit}>
		<button class="btn btn-primary transition duration-300">
			{#if $isLoading}
				<span class="loading loading-spinner text-info" />
			{:else}
				{$_('page.game.mode-selection.task')}
			{/if}
		</button>
	</form>
</div>
