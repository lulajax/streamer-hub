<template>
  <button :class="classes" :type="type" :disabled="disabled" v-bind="$attrs">
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link'
    size?: 'default' | 'sm' | 'lg' | 'icon'
    type?: 'button' | 'submit' | 'reset'
    disabled?: boolean
  }>(),
  {
    variant: 'default',
    size: 'default',
    type: 'button',
    disabled: false,
  }
)

const base =
  'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50'

const variantClasses: Record<string, string> = {
  default: 'bg-blue-600 text-white hover:bg-blue-600/90',
  destructive: 'bg-red-600 text-white hover:bg-red-600/90',
  outline: 'border border-slate-700 bg-transparent text-slate-200 hover:bg-slate-800',
  secondary: 'bg-slate-700 text-white hover:bg-slate-700/80',
  ghost: 'hover:bg-slate-800 text-slate-200',
  link: 'text-blue-400 underline-offset-4 hover:underline',
}

const sizeClasses: Record<string, string> = {
  default: 'h-10 px-4 py-2',
  sm: 'h-9 rounded-md px-3',
  lg: 'h-11 rounded-md px-8',
  icon: 'h-10 w-10',
}

const classes = computed(() => [
  base,
  variantClasses[props.variant],
  sizeClasses[props.size],
])
</script>
