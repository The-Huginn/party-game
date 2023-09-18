import { setupI18n } from '$lib/i18n/i18n-init'
import type { LayoutLoad } from './$types'

setupI18n();

export const ssr = false;

export const load: LayoutLoad = ({ url }) => {
  return { url: url.pathname }
}
