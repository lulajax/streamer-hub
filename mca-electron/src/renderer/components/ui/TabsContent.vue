<template>
  <div v-show="isActive">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'

const props = defineProps<{ value: string }>()

const tabs = inject<{ activeValue: { value: string } }>('tabs')

const isActive = computed(() => tabs?.activeValue.value === props.value)
</script>
