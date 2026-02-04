<template>
  <div v-bind="$attrs">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, provide, ref, watch } from 'vue'
import type { SelectContext } from './select-context'
import { selectContextKey } from './select-context'

const props = defineProps<{
  modelValue?: string
  defaultValue?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const open = ref(false)
const value = ref(props.modelValue ?? props.defaultValue ?? '')
const triggerRef = ref<HTMLElement | null>(null)
const contentRef = ref<HTMLElement | null>(null)
const labels = ref<Record<string, string>>({})

watch(
  () => props.modelValue,
  (val) => {
    if (val !== undefined) value.value = val
  }
)

const setOpen = (next: boolean) => {
  open.value = next
}

const setValue = (next: string) => {
  value.value = next
  emit('update:modelValue', next)
  open.value = false
}

const registerItem = (itemValue: string, label: string) => {
  if (labels.value[itemValue] === label) return
  labels.value = { ...labels.value, [itemValue]: label }
}

const getLabel = (itemValue: string) => labels.value[itemValue] ?? ''

const handlePointerDown = (event: MouseEvent) => {
  if (!open.value) return
  const target = event.target as Node
  if (triggerRef.value?.contains(target) || contentRef.value?.contains(target)) return
  open.value = false
}

const handleKeyDown = (event: KeyboardEvent) => {
  if (!open.value) return
  if (event.key === 'Escape') {
    open.value = false
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handlePointerDown)
  document.addEventListener('keydown', handleKeyDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handlePointerDown)
  document.removeEventListener('keydown', handleKeyDown)
})

const context: SelectContext = {
  open,
  value,
  triggerRef,
  contentRef,
  setOpen,
  setValue,
  registerItem,
  getLabel,
}

provide(selectContextKey, context)
</script>
