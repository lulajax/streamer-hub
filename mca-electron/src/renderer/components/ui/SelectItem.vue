<template>
  <div
    ref="itemEl"
    role="option"
    :aria-selected="isSelected"
    :data-disabled="disabled ? '' : undefined"
    :class="cn(
      'relative flex w-full cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
      $attrs.class || ''
    )"
    @click="handleSelect"
    v-bind="attrsWithoutClass"
  >
    <span class="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
      <Check v-if="isSelected" class="h-4 w-4" />
    </span>
    <span>
      <slot />
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed, inject, onMounted, ref, useAttrs } from 'vue'
import { Check } from 'lucide-vue-next'
import { cn } from '@/lib/cn'
import type { SelectContext } from './select-context'
import { selectContextKey } from './select-context'

const props = defineProps<{
  value: string
  disabled?: boolean
}>()

const context = inject(selectContextKey) as SelectContext | null
const attrs = useAttrs()
const itemEl = ref<HTMLElement | null>(null)

const attrsWithoutClass = computed(() => {
  const { class: _class, ...rest } = attrs
  return rest
})

const isSelected = computed(() => context?.value.value === props.value)

const handleSelect = () => {
  if (!context || props.disabled) return
  context.setValue(props.value)
}

onMounted(() => {
  if (!context || !itemEl.value) return
  const label = itemEl.value.textContent?.trim() || ''
  context.registerItem(props.value, label)
})
</script>
