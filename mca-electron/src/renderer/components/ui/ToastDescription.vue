<template>
  <div :class="cn('text-sm opacity-90', $attrs.class || '')" v-bind="$attrs">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
