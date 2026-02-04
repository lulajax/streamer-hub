<template>
  <slot />
</template>

<script setup lang="ts">
import { computed, provide } from 'vue'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ (event: 'update:open', value: boolean): void }>()

const isOpen = computed(() => props.open)

const setOpen = (value: boolean) => {
  emit('update:open', value)
}

provide('dialog', { isOpen, setOpen })
</script>
