<template>
  <div :class="cn('flex cursor-default items-center justify-center py-1', $attrs.class || '')" v-bind="$attrs">
    <ChevronDown class="h-4 w-4" />
  </div>
</template>

<script setup lang="ts">
import { ChevronDown } from 'lucide-vue-next'
import { cn } from '@/lib/cn'
</script>
