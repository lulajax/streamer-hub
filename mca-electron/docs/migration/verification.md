# Migration Verification

## Screenshot Diff (manual or scripted)
1. Run the legacy React build and the Vue3 build with identical data fixtures.
2. Capture screenshots at fixed viewports:
   - Desktop: 1920x1080, 1440x900, 1366x768
   - DPR: 1
3. Keep environment fixed:
   - Language: en
   - Timezone: UTC
   - Font: default system (no overrides)
4. Compare images and confirm no pixel diffs.

## E2E (Playwright)
- Start the renderer dev server:
  - npm run dev
- Run tests:
  - npx playwright test -c e2e/playwright.config.ts

## Expected Coverage
- Widgets page: copy link + open widget action
- Reports page: session list + detail tab
- Settings page: language/theme select + activation code redeem flow
