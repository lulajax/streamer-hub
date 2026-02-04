<template>
  <img
    :src="src"
    :alt="alt || ''"
    :class="cn('aspect-square h-full w-full', $attrs.class || '')"
    @error="$emit('error', $event)"
  />
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'

defineProps<{
  src: string
  alt?: string
}>()

defineEmits<{
  (e: 'error', event: Event): void
}>()
</script>
