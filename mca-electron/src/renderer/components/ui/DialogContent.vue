<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-black/60" @click="close"></div>
      <div class="relative z-10 w-full max-w-lg rounded-lg border border-slate-800 bg-slate-900 p-6">
        <slot />
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'

const dialog = inject<{ isOpen: { value: boolean }; setOpen: (value: boolean) => void }>(
  'dialog'
)

const isOpen = computed(() => dialog?.isOpen.value)

const close = () => dialog?.setOpen(false)
</script>
