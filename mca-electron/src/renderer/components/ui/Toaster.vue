<template>
  <ToastProvider>
    <ToastViewport>
      <TransitionGroup
        enter-active-class="animate-in fade-in-0 slide-in-from-top-full sm:slide-in-from-bottom-full"
        leave-active-class="animate-out fade-out-80 slide-out-to-right-full"
      >
        <Toast
          v-for="toast in toasts"
          :key="toast.id"
          :variant="toast.variant"
        >
          <div class="grid gap-1">
            <ToastTitle v-if="toast.title">{{ toast.title }}</ToastTitle>
            <ToastDescription v-if="toast.description">{{ toast.description }}</ToastDescription>
          </div>
          <ToastClose @click="dismiss(toast.id)" />
        </Toast>
      </TransitionGroup>
    </ToastViewport>
  </ToastProvider>
</template>

<script setup lang="ts">
import { useToast, toasts } from '@/hooks/use-toast'
import {
  Toast,
  ToastClose,
  ToastDescription,
  ToastProvider,
  ToastTitle,
  ToastViewport,
} from './toast'

const { dismiss } = useToast()
</script>
