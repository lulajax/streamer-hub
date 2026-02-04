<template>
  <td :class="cn('p-4 align-middle [&:has([role=checkbox])]:pr-0', $attrs.class || '')" v-bind="$attrs">
    <slot />
  </td>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
