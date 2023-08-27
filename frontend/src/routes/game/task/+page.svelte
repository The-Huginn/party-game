<script lang="ts">
	import { goto, invalidateAll } from '$app/navigation';
	import Alert from '$lib/components/Alert.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { isLoading } from 'svelte-i18n';
	import { header_text } from '../../../store';
	import type { PageData } from './$types';
	import Task from '$lib/components/Task.svelte';

	export let data: PageData;

	export let { rawTask } = data;
	export let { task } = data;
	console.log(rawTask[task.task]);

	let unique = {};

	async function handleSubmit(event) {
		unique = {};
	}
</script>

{#key unique}
	<Task handleSubmit={handleSubmit}/>
{/key}
