<template>
  <button
    type="button"
    :class="cn(
      'peer inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50',
      isChecked ? 'bg-primary' : 'bg-input',
      $attrs.class || ''
    )"
    :data-state="isChecked ? 'checked' : 'unchecked'"
    :aria-checked="isChecked"
    role="switch"
    :disabled="disabled"
    @click="toggle"
    v-bind="attrsWithoutClass"
  >
    <span
      :class="cn(
        'pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform',
        isChecked ? 'translate-x-5' : 'translate-x-0'
      )"
      :data-state="isChecked ? 'checked' : 'unchecked'"
    />
  </button>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from '@/lib/cn'

const props = defineProps<{
  modelValue?: boolean
  checked?: boolean
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'update:checked', value: boolean): void
}>()

const attrs = useAttrs()

const attrsWithoutClass = computed(() => {
  const { class: _class, ...rest } = attrs
  return rest
})

const isChecked = computed(() => props.modelValue ?? props.checked ?? false)

const toggle = () => {
  if (props.disabled) return
  const next = !isChecked.value
  emit('update:modelValue', next)
  emit('update:checked', next)
}
</script>
