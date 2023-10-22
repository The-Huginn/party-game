<script lang="ts">
	import TaskMode from "./TaskMode.svelte";
	import type { PageData } from './$types';
	import PubMode from "./PubMode.svelte";

	export let data: PageData;

</script>

{#if data.type == 'TASK'}
	<TaskMode data={data.data}/>
{:else if data.type == 'PUB' }
	<PubMode data={data.data}/>
{/if}


