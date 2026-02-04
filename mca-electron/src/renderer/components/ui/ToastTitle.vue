<template>
  <div :class="cn('text-sm font-semibold', $attrs.class || '')" v-bind="$attrs">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
