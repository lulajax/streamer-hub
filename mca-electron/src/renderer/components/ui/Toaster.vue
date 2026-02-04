<template>
  <div class="fixed right-4 top-4 z-50 flex w-full max-w-sm flex-col gap-2">
    <Toast
      v-for="item in state.toasts"
      :key="item.id"
      :toast="item"
      @dismiss="dismiss(item.id)"
    />
  </div>
</template>

<script setup lang="ts">
import { useToast } from '@/hooks/use-toast'
import Toast from './Toast.vue'

const { state, dismiss } = useToast()
</script>
