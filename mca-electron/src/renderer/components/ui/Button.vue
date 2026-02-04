<template>
  <button
    :class="cn(buttonVariants({ variant, size }), $attrs.class || '')"
    :disabled="disabled"
    v-bind="$attrs"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
import { buttonVariants } from './button'
import type { ButtonProps } from './button'

withDefaults(defineProps<ButtonProps>(), {
  variant: 'default',
  size: 'default',
  disabled: false,
})
</script>
