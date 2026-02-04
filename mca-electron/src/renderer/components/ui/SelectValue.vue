<template>
  <span>
    {{ label || placeholder || '' }}
  </span>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'
import type { SelectContext } from './select-context'
import { selectContextKey } from './select-context'

const props = defineProps<{
  placeholder?: string
}>()

const context = inject(selectContextKey) as SelectContext | null

const label = computed(() => {
  if (!context) return ''
  return context.getLabel(context.value.value)
})
</script>
