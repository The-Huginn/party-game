<script lang="ts">
	import App from '../App.svelte';
	import Header from './Header.svelte';
	import './styles.css';

	import type { PageData } from './$types';
	import PageTransition from './transition.svelte';

	export let data: PageData;
</script>

<ul class="circles">
	<li class="bg-primary" />
	<li class="bg-secondary" />
	<li class="bg-accent" />
	<li class="bg-primary" />
	<li class="bg-secondary" />
	<li class="bg-accent" />
	<li class="bg-primary" />
	<li class="bg-secondary" />
	<li class="bg-accent" />
	<li class="bg-primary" />
</ul>
<div class="flex flex-col min-h-screen">
	<App />
	<Header />
	<main
		class="flex flex-1 flex-col justify-center items-center p-1 w-full max-w-64 m-0 box-border mt-10"
	>
		<PageTransition key={data.url} duration={600}>
			<slot />
		</PageTransition>
	</main>

	<footer class="flex flex-col justify-center items-center p-3">
		<p class="font-bold">&copy; 2023 Huginn</p>
	</footer>
</div>

<style>
	@media (min-width: 480px) {
		footer {
			padding: 12px 0;
		}
	}

	.circles {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		overflow: hidden;
	}

	.circles li {
		position: absolute;
		display: block;
		list-style: none;
		width: 20px;
		height: 20px;
		animation: animate 25s linear infinite;
		bottom: -150px;
	}

	.circles li:nth-child(1) {
		left: 25%;
		width: 80px;
		height: 80px;
		animation-delay: 0s;
	}

	.circles li:nth-child(2) {
		left: 10%;
		width: 20px;
		height: 20px;
		animation-delay: 2s;
		animation-duration: 12s;
	}

	.circles li:nth-child(3) {
		left: 70%;
		width: 20px;
		height: 20px;
		animation-delay: 4s;
	}

	.circles li:nth-child(4) {
		left: 40%;
		width: 60px;
		height: 60px;
		animation-delay: 0s;
		animation-duration: 18s;
	}

	.circles li:nth-child(5) {
		left: 65%;
		width: 20px;
		height: 20px;
		animation-delay: 0s;
	}

	.circles li:nth-child(6) {
		left: 75%;
		width: 110px;
		height: 110px;
		animation-delay: 3s;
	}

	.circles li:nth-child(7) {
		left: 35%;
		width: 150px;
		height: 150px;
		animation-delay: 7s;
	}

	.circles li:nth-child(8) {
		left: 50%;
		width: 25px;
		height: 25px;
		animation-delay: 15s;
		animation-duration: 45s;
	}

	.circles li:nth-child(9) {
		left: 20%;
		width: 15px;
		height: 15px;
		animation-delay: 2s;
		animation-duration: 35s;
	}

	.circles li:nth-child(10) {
		left: 85%;
		width: 150px;
		height: 150px;
		animation-delay: 0s;
		animation-duration: 11s;
	}

	@keyframes animate {
		0% {
			transform: translateY(0) rotate(0deg);
			opacity: 1;
			border-radius: 0;
		}

		100% {
			transform: translateY(-1000px) rotate(720deg);
			opacity: 0;
			border-radius: 50%;
		}
	}
</style>
