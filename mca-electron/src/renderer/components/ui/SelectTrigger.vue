<template>
  <button
    ref="triggerEl"
    type="button"
    :class="cn(
      'flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 [&>span]:line-clamp-1',
      $attrs.class || ''
    )"
    :aria-expanded="context?.open.value || false"
    :disabled="disabled"
    @click="toggle"
    v-bind="attrsWithoutClass"
  >
    <slot />
    <ChevronDown class="h-4 w-4 opacity-50" />
  </button>
</template>

<script setup lang="ts">
import { computed, inject, onMounted, ref, useAttrs } from 'vue'
import { ChevronDown } from 'lucide-vue-next'
import { cn } from '@/lib/cn'
import type { SelectContext } from './select-context'
import { selectContextKey } from './select-context'

const props = defineProps<{
  disabled?: boolean
}>()

const attrs = useAttrs()
const attrsWithoutClass = computed(() => {
  const { class: _class, ...rest } = attrs
  return rest
})

const context = inject(selectContextKey) as SelectContext | null
const triggerEl = ref<HTMLElement | null>(null)

const toggle = () => {
  if (!context || props.disabled) return
  context.setOpen(!context.open.value)
}

onMounted(() => {
  if (context) context.triggerRef.value = triggerEl.value
})
</script>
