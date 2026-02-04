import { reactive, readonly } from 'vue'

export type ToastVariant = 'default' | 'destructive'

export interface ToastOptions {
  title?: string
  description?: string
  variant?: ToastVariant
  duration?: number
}

export interface ToastItem extends ToastOptions {
  id: string
  open: boolean
}

const TOAST_LIMIT = 1
const TOAST_REMOVE_DELAY = 3000

const state = reactive<{ toasts: ToastItem[] }>({
  toasts: [],
})

let count = 0
const toastTimeouts = new Map<string, ReturnType<typeof setTimeout>>()

const genId = () => {
  count = (count + 1) % Number.MAX_SAFE_INTEGER
  return count.toString()
}

const removeToast = (toastId?: string) => {
  if (!toastId) {
    state.toasts = []
    return
  }
  state.toasts = state.toasts.filter((toast) => toast.id !== toastId)
}

const addToRemoveQueue = (toastId: string, delay: number) => {
  if (toastTimeouts.has(toastId)) return
  const timeout = setTimeout(() => {
    toastTimeouts.delete(toastId)
    removeToast(toastId)
  }, delay)
  toastTimeouts.set(toastId, timeout)
}

export function toast(options: ToastOptions) {
  const id = genId()
  const duration = options.duration ?? TOAST_REMOVE_DELAY

  state.toasts = [
    {
      id,
      open: true,
      ...options,
    },
    ...state.toasts,
  ].slice(0, TOAST_LIMIT)

  addToRemoveQueue(id, duration)

  return {
    id,
    dismiss: () => dismiss(id),
  }
}

export function dismiss(toastId?: string) {
  if (toastId) {
    state.toasts = state.toasts.map((toast) =>
      toast.id === toastId ? { ...toast, open: false } : toast
    )
    addToRemoveQueue(toastId, 0)
    return
  }

  state.toasts.forEach((toastItem) => addToRemoveQueue(toastItem.id, 0))
  state.toasts = state.toasts.map((toastItem) => ({ ...toastItem, open: false }))
}

export function useToast() {
  return {
    state: readonly(state),
    toast,
    dismiss,
  }
}
