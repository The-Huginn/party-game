<script lang="ts">
	import { goto } from '$app/navigation';
	import Modal from '$lib/components/Modal.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { isLoading } from 'svelte-i18n';
	import { game_url, header } from '../../../store';
	import type { PageData } from './$types';

	export let formSuccess: boolean = true;
	export let data: PageData;
	$header = { text: 'page.game.mode-selection.title', append: '' };

	async function handleSubmit(event: SubmitEvent) {
		const formDatam = new FormData(this);

		const response = await fetch(`${game_url}/mode/create`, {
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

{#if data.status == 200}
	<Modal
		onMountCallback={() => {}}
		yesCallback={() => goto('/game/task')}
		noCallback={() => {}}
		question="page.game.mode-selection.game_continue"
	/>
{/if}
<div class="flex flex-col w-4/5 lg:w-2/5 items-center space-y-5">
	<form class="w-full flex flex-col space-y-5" on:submit|preventDefault={handleSubmit}>
		<button class="btn btn-primary transition duration-300 min-h-16 text-xl">
			{#if $isLoading}
				<span class="loading loading-spinner text-info" />
			{:else}
				{$_('page.game.mode-selection.task')}
			{/if}
		</button>
	</form>
</div>
