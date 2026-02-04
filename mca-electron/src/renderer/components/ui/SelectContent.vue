<template>
  <Teleport to="body">
    <div
      v-if="context?.open.value"
      ref="contentEl"
      :class="cn(
        'relative z-50 max-h-96 min-w-[8rem] overflow-hidden rounded-md border bg-popover text-popover-foreground shadow-md animate-in fade-in-0 zoom-in-95',
        $attrs.class || ''
      )"
      :style="contentStyle"
      v-bind="attrsWithoutClass"
    >
      <div class="p-1">
        <slot />
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, inject, onMounted, ref, useAttrs, watch } from 'vue'
import { cn } from '@/lib/cn'
import type { SelectContext } from './select-context'
import { selectContextKey } from './select-context'

const context = inject(selectContextKey) as SelectContext | null
const attrs = useAttrs()
const contentEl = ref<HTMLElement | null>(null)

const attrsWithoutClass = computed(() => {
  const { class: _class, style: _style, ...rest } = attrs
  return rest
})

const contentStyle = computed(() => {
  const trigger = context?.triggerRef.value
  if (!trigger) return attrs.style as Record<string, string> | undefined
  const rect = trigger.getBoundingClientRect()
  return {
    position: 'fixed',
    top: `${rect.bottom + 4}px`,
    left: `${rect.left}px`,
    minWidth: `${rect.width}px`,
    ...(attrs.style as Record<string, string> | undefined),
  }
})

onMounted(() => {
  if (context) context.contentRef.value = contentEl.value
})

watch(
  () => context?.open.value,
  (val) => {
    if (!val || !context || !context.triggerRef.value || !contentEl.value) return
    const rect = context.triggerRef.value.getBoundingClientRect()
    contentEl.value.style.top = `${rect.bottom + 4}px`
    contentEl.value.style.left = `${rect.left}px`
    contentEl.value.style.minWidth = `${rect.width}px`
  }
)
</script>
