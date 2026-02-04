<template>
  <div :class="cn('relative overflow-hidden', $attrs.class || '')" v-bind="$attrs">
    <div class="h-full w-full rounded-[inherit] overflow-auto">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
