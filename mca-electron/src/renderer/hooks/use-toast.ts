import { ref } from 'vue'

export interface Toast {
  id: string
  title?: string
  description?: string
  variant?: 'default' | 'destructive'
  duration?: number
}

const toasts = ref<Toast[]>([])

export function useToast() {
  const toast = (props: Omit<Toast, 'id'>) => {
    const id = Math.random().toString(36).substring(2, 9)
    const newToast: Toast = {
      id,
      duration: 5000,
      ...props,
    }
    toasts.value.push(newToast)

    setTimeout(() => {
      dismiss(id)
    }, newToast.duration)

    return id
  }

  const dismiss = (id: string) => {
    const index = toasts.value.findIndex((t) => t.id === id)
    if (index > -1) {
      toasts.value.splice(index, 1)
    }
  }

  return {
    toast,
    dismiss,
    toasts,
  }
}

export { toasts }
