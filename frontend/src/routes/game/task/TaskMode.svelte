<script lang="ts">
    import { page } from '$app/stores';
	import Alert from '$lib/components/Alert.svelte';
	import Loading from '$lib/components/Loading.svelte';
	import { _ } from '$lib/i18n/i18n-init';
	import { onDestroy } from 'svelte';
	import { isLoading, locale } from 'svelte-i18n';
	import { game_url, header } from '../../../store';
	import PairTable from './PairTable.svelte';
	import Price from './Price.svelte';
	import type { Task, Timer } from './Task';
	import TimerComponent from './TimerComponent.svelte';
	import type { PageData } from '../../$types';
    
    export let data: PageData;
    
    let formSuccess: string = '';
	$: rawTask = data;
    $: task = rawTask as Task;
	$: timer = data.timer as Timer;
	$: initialLoad = true;
    let nextCallback;
    
    function submitHandler(event: SubmitEvent) {
        nextCallback = nextTask(event);
    }

    async function nextTask(event: SubmitEvent) {
        const response = await fetch(`${game_url}/mode/next`, {
            method: 'PUT',
            credentials: 'include'
        });

        data = (await response.json()).data,
        initialLoad = true;
        return;
    }

    $: subscription = locale.subscribe(async (newLocale) => {
        if (newLocale == null) {
            return;
        }

        const response = await fetch(`${game_url}/mode/current`, {
            method: 'GET',
            credentials: 'include'
        });

        rawTask = (await response.json()).data as Task;
    });

    $: onDestroy(subscription);
    $: {
        $header.text = 'page.game.task.' + task.task_type.toLowerCase();
        if (task.task_type == 'SINGLE') {
            $header.append = task.player;
        } else {
            $header.append = '';
        }
    }
</script>

<div class="flex flex-col w-full items-center justify-center space-y-5">
{#if task.pairs}
    <PairTable pairs={task.pairs} />
{/if}
<div
    class="grid relative w-4/5 lg:w-2/5 gap-4 p-4 mb-4 bg-info shadow-lg border-1 border-solid border-gray-800 rounded-2xl"
>
    {#if task.price.enabled}
        <Price price={task.price.price} />
    {/if}
    <h1 class="pt-4">
        <span class="font-bold text-4xl">
            {#await nextCallback}
                <Loading />
            {:then}
                {@html rawTask[task.task]}
            {/await}
        </span>
    </h1>
    {#if !initialLoad && timer}
        <progress value={timer.duration / timer.initialDuration} />
    {/if}
</div>
{#key timer}
    <TimerComponent {timer} />
{/key}
<form on:submit|preventDefault={submitHandler}>
    <button class="btn btn-primary transition duration-300 min-h-16 text-3xl">
        {#if $isLoading}
            <span class="loading loading-spinner text-info" />
        {:else}
            {$_('page.game.task.next')}
        {/if}
    </button>
    {#if formSuccess != ''}
        <Alert message={formSuccess} />
    {/if}
</form>
</div>