<template>
  <div
    :class="cn(toastVariants({ variant }), $attrs.class || '')"
    data-state="open"
    v-bind="$attrs"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
import { toastVariants, type ToastVariant } from './toast'

withDefaults(
  defineProps<{
    variant?: ToastVariant
  }>(),
  {
    variant: 'default',
  }
)
</script>
