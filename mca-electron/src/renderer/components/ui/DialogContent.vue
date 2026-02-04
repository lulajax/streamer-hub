<template>
  <Teleport to="body">
    <Transition enter-active-class="animate-in fade-in-0" leave-active-class="animate-out fade-out-0">
      <div
        v-if="isOpen"
        class="fixed inset-0 z-50 bg-black/80"
        @click="close"
      />
    </Transition>
    <Transition
      enter-active-class="animate-in fade-in-0 zoom-in-95 slide-in-from-left-1/2 slide-in-from-top-[48%]"
      leave-active-class="animate-out fade-out-0 zoom-out-95 slide-out-to-left-1/2 slide-out-to-top-[48%]"
    >
      <div
        v-if="isOpen"
        :class="cn(
          'fixed left-[50%] top-[50%] z-50 grid w-full max-w-lg translate-x-[-50%] translate-y-[-50%] gap-4 border bg-background p-6 shadow-lg duration-200 sm:rounded-lg',
          $attrs.class || ''
        )"
        v-bind="$attrs"
      >
        <slot />
        <button
          type="button"
          class="absolute right-4 top-4 rounded-sm opacity-70 ring-offset-background transition-opacity hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:pointer-events-none data-[state=open]:bg-accent data-[state=open]:text-muted-foreground"
          @click="close"
        >
          <X class="h-4 w-4" />
          <span class="sr-only">Close</span>
        </button>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'
import { X } from 'lucide-vue-next'
import { cn } from '@/lib/cn'

const open = inject('dialogOpen') as { value?: boolean } | undefined
const setOpen = inject('setDialogOpen') as ((value: boolean) => void) | undefined

const isOpen = computed(() => Boolean(open?.value))

const close = () => {
  if (setOpen) setOpen(false)
}
</script>
