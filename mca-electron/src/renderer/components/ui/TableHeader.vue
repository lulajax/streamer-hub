<template>
  <thead :class="cn('[&_tr]:border-b', $attrs.class || '')" v-bind="$attrs">
    <slot />
  </thead>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
