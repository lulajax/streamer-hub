<template>
  <div :class="cn('-mx-1 my-1 h-px bg-muted', $attrs.class || '')" v-bind="$attrs" />
</template>

<script setup lang="ts">
import { cn } from '@/lib/cn'
</script>
