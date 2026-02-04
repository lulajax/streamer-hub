<template>
  <div>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed, provide, ref, watch } from 'vue'

const props = defineProps<{
  modelValue?: string
  defaultValue?: string
}>()

const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>()

const internalValue = ref(props.modelValue ?? props.defaultValue ?? '')

watch(
  () => props.modelValue,
  (value) => {
    if (value !== undefined) {
      internalValue.value = value
    }
  }
)

const setValue = (value: string) => {
  internalValue.value = value
  emit('update:modelValue', value)
}

const activeValue = computed(() => internalValue.value)

provide('tabs', {
  activeValue,
  setValue,
})
</script>
