<template>
  <div :class="cn('py-1.5 pl-8 pr-2 text-sm font-semibold', $attrs.class || '')" v-bind="$attrs">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
