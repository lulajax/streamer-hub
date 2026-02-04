<template>
  <span :class="classes">
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{ variant?: 'default' | 'secondary' }>(),
  { variant: 'default' }
)

const base = 'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold'

const variantClasses: Record<string, string> = {
  default: 'border-slate-700 text-slate-200 bg-slate-900',
  secondary: 'border-slate-700 text-slate-300 bg-slate-800',
}

const classes = computed(() => [base, variantClasses[props.variant]])
</script>
