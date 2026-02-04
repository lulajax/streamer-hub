<template>
  <div
    v-if="toast.open"
    :class="[
      'pointer-events-auto w-full max-w-sm rounded-lg border p-4 shadow-lg',
      toast.variant === 'destructive'
        ? 'bg-red-900/90 border-red-700 text-red-50'
        : 'bg-slate-900 border-slate-800 text-slate-100',
    ]"
  >
    <div class="flex items-start justify-between gap-4">
      <div class="space-y-1">
        <p v-if="toast.title" class="text-sm font-semibold">{{ toast.title }}</p>
        <p v-if="toast.description" class="text-sm text-slate-300">{{ toast.description }}</p>
      </div>
      <button
        class="text-slate-400 hover:text-slate-200"
        type="button"
        @click="emit('dismiss')"
      >
        ×
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ToastItem } from '@/hooks/use-toast'

defineProps<{ toast: ToastItem }>()

const emit = defineEmits<{ (event: 'dismiss'): void }>()
</script>
