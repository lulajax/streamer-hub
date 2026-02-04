<template>
  <div :class="cn('relative flex w-full touch-none select-none items-center', $attrs.class || '')" v-bind="attrsWithoutClass">
    <div class="relative h-2 w-full grow overflow-hidden rounded-full bg-slate-800">
      <div class="absolute h-full bg-primary" :style="{ width: `${percent}%` }" />
    </div>
    <div
      class="absolute top-1/2 -translate-y-1/2"
      :style="{ left: `calc(${percent}% - 10px)` }"
    >
      <div class="block h-5 w-5 rounded-full border-2 border-primary bg-background ring-offset-background transition-colors" />
    </div>
    <input
      type="range"
      class="absolute inset-0 h-full w-full cursor-pointer opacity-0 disabled:cursor-not-allowed"
      :min="min"
      :max="max"
      :step="step"
      :value="currentValue"
      :disabled="disabled"
      @input="onInput"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from '@/lib/cn'

const props = withDefaults(
  defineProps<{
    modelValue?: number | number[]
    value?: number
    min?: number
    max?: number
    step?: number
    disabled?: boolean
  }>(),
  {
    min: 0,
    max: 100,
    step: 1,
    disabled: false,
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: number | number[]): void
}>()

const attrs = useAttrs()

const attrsWithoutClass = computed(() => {
  const { class: _class, ...rest } = attrs
  return rest
})

const currentValue = computed(() => {
  if (Array.isArray(props.modelValue)) {
    return props.modelValue[0] ?? props.min
  }
  if (props.modelValue !== undefined) return props.modelValue
  if (props.value !== undefined) return props.value
  return props.min
})

const percent = computed(() => {
  const range = props.max - props.min
  if (range <= 0) return 0
  return ((currentValue.value - props.min) / range) * 100
})

const onInput = (event: Event) => {
  const next = Number((event.target as HTMLInputElement).value)
  const payload = Array.isArray(props.modelValue) ? [next] : next
  emit('update:modelValue', payload)
}
</script>
