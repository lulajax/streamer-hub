<template>
  <div v-bind="$attrs">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { provide, ref, watch } from 'vue'

const props = defineProps<{
  modelValue?: string
  defaultValue?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const activeTab = ref(props.modelValue ?? props.defaultValue ?? '')

watch(() => props.modelValue, (val) => {
  if (val !== undefined) activeTab.value = val
})

watch(activeTab, (val) => {
  emit('update:modelValue', val)
})

provide('activeTab', activeTab)
provide('setActiveTab', (val: string) => {
  activeTab.value = val
})
</script>
