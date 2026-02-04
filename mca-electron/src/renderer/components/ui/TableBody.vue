<template>
  <tbody :class="cn('[&_tr:last-child]:border-0', $attrs.class || '')" v-bind="$attrs">
    <slot />
  </tbody>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
