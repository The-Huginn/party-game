<script lang="ts">
	import { onMount } from 'svelte';
	import { _, isLoading } from 'svelte-i18n';

	export let onMountCallback: Function;
	export let yesCallback: Function;
	export let noCallback: Function;
	export let question: string;
	$: modalShow = true;

	onMount(async () => {
		onMountCallback();
	});

	function handleSubmit(event: SubmitEvent) {
		modalShow = false;
		if (event.submitter?.id == 'yes') {
			yesCallback();
		} else {
			noCallback();
		}
	}

	function keyPress(ev: KeyboardEvent) {
		if (ev.key == 'Escape') {
			modalShow = false;
			noCallback();
		}
	}
	window.addEventListener('keydown', keyPress);
</script>

{#if modalShow}
	<input type="checkbox" id="modal" class="modal-toggle" checked />
	<div class="modal">
		<div class="modal-box items-center justify-center">
			<p class="text-xl text-center font-medium">
				{#if $isLoading}
					<span class="loading loading-spinner text-info" />
				{:else}
					{$_(question)}
				{/if}
			</p>
			<form on:submit|preventDefault={handleSubmit}>
				<div class="modal-action">
					<button class="btn btn-primary text-xl" id="yes">yes</button>
					<button class="btn text-xl" id="no">no</button>
				</div>
			</form>
		</div>
	</div>
{/if}
