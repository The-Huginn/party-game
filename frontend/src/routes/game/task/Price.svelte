<script lang="ts">
	import shot from '$lib/images/shot.svg';
	import { _ } from 'svelte-i18n';

    export let price: number;
</script>

<div class="absolute flex flex-row w-full justify-end whitespace-nowrap -mt-6 mx-4">
    <div class="flex flex-row float-right items-center justify-center bg-warning rounded-2xl p-2 shadow-lg">
        <p class="text-xl font-bold">{$_('page.game.task.price')}</p>
        {#each Array(price) as _}
            <img class="object-contain w-8 h-8" src={shot} alt="price" />
        {/each}
    </div>
</div>