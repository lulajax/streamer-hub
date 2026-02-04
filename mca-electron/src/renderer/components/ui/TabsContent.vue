<template>
  <div
    v-if="isActive"
    :class="cn('mt-2 ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2', $attrs.class || '')"
    v-bind="$attrs"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'
import { cn } from '@/lib/cn'

const props = defineProps<{
  value: string
}>()

const activeTab = inject('activeTab') as any

const isActive = computed(() => activeTab.value === props.value)
</script>
