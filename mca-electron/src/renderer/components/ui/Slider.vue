<template>
  <input
    type="range"
    :min="min"
    :max="max"
    :step="step"
    :value="modelValue"
    class="w-full accent-blue-500"
    @input="onInput"
  />
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue?: number
    min?: number
    max?: number
    step?: number
  }>(),
  {
    modelValue: 0,
    min: 0,
    max: 100,
    step: 1,
  }
)

const emit = defineEmits<{ (event: 'update:modelValue', value: number): void }>()

const onInput = (event: Event) => {
  emit('update:modelValue', Number((event.target as HTMLInputElement).value))
}
</script>
