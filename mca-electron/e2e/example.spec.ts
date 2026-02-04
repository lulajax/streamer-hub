import { test, expect } from '@playwright/test'

test('loads the MCA shell', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByText('MCA')).toBeVisible()
})
