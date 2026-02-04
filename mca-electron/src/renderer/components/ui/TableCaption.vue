<template>
  <caption :class="cn('mt-4 text-sm text-muted-foreground', $attrs.class || '')" v-bind="$attrs">
    <slot />
  </caption>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
