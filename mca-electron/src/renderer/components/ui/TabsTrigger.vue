<template>
  <button
    type="button"
    :class="[
      'rounded-md px-3 py-1.5 text-sm font-medium transition',
      isActive ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-slate-200',
    ]"
    @click="setValue(value)"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'

const props = defineProps<{ value: string }>()

const tabs = inject<{ activeValue: { value: string }; setValue: (value: string) => void }>(
  'tabs'
)

const isActive = computed(() => tabs?.activeValue.value === props.value)

const setValue = (value: string) => {
  tabs?.setValue(value)
}
</script>
