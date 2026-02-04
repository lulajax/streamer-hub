import type { Ref } from 'vue'

export interface SelectContext {
  open: Ref<boolean>
  value: Ref<string>
  triggerRef: Ref<HTMLElement | null>
  contentRef: Ref<HTMLElement | null>
  setOpen: (value: boolean) => void
  setValue: (value: string) => void
  registerItem: (value: string, label: string) => void
  getLabel: (value: string) => string
}

export const selectContextKey = Symbol('selectContext')
