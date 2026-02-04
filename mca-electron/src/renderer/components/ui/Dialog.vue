<template>
  <slot />
</template>

<script setup lang="ts">
import { computed, provide, ref, watch } from 'vue'

const props = defineProps<{
  open?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const internalOpen = ref(props.open ?? false)

watch(
  () => props.open,
  (val) => {
    if (val !== undefined) internalOpen.value = val
  }
)

const open = computed(() => (props.open === undefined ? internalOpen.value : props.open))

const setOpen = (value: boolean) => {
  if (props.open === undefined) internalOpen.value = value
  emit('update:open', value)
}

provide('dialogOpen', open)
provide('setDialogOpen', setOpen)
</script>
