<template>
  <button
    :class="cn(
      'inline-flex items-center justify-center whitespace-nowrap rounded-sm px-3 py-1.5 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
      isActive ? 'bg-background text-foreground shadow-sm' : '',
      $attrs.class || ''
    )"
    :data-state="isActive ? 'active' : 'inactive'"
    @click="setActiveTab(value)"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'
import { cn } from '@/lib/cn'

const props = defineProps<{
  value: string
}>()

const activeTab = inject('activeTab') as any
const setActiveTab = inject('setActiveTab') as (val: string) => void

const isActive = computed(() => activeTab.value === props.value)
</script>
