import { getCookie } from '$lib/common/cookies';
import en from '$lib/i18n/en.json';
import sk from '$lib/i18n/sk.json';
import { _, addMessages, getLocaleFromNavigator, init } from 'svelte-i18n';

function setupI18n({ withLocale: _locale } = { withLocale: getCookie('locale') ?? getLocaleFromNavigator() }) {
    // register('en', () => import('./en.json'));
    // register('en', () => import('./sk.json'));

    addMessages('en', en);
    addMessages('sk', sk);

    init({
        fallbackLocale: 'en',
        initialLocale: _locale
    });
}

export { _, setupI18n };
