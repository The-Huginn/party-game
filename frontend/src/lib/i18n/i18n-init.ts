import { _, addMessages, getLocaleFromNavigator, init, register } from 'svelte-i18n';
import en from '$lib/i18n/en.json';
import sk from '$lib/i18n/sk.json';

function setupI18n({ withLocale: _locale } = { withLocale: getLocaleFromNavigator() }) {
    // register('en', () => import('./en.json'));
    // register('en', () => import('./sk.json'));
    
    addMessages('en-US', en);
    addMessages('sk-SK', sk);
    
    init({
        fallbackLocale: 'en-US',
        initialLocale: _locale
    });
}

export { _, setupI18n };