<template>
  <div class="relative w-full overflow-auto">
    <table :class="cn('w-full caption-bottom text-sm', $attrs.class || '')" v-bind="$attrs">
      <slot />
    </table>
  </div>
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
