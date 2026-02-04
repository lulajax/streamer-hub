<template>
  <input
    :type="type"
    :value="modelValue"
    :placeholder="placeholder"
    :class="[
      'flex h-10 w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 focus-visible:ring-offset-2',
      $attrs.class,
    ]"
    @input="onInput"
  />
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue?: string | number
    placeholder?: string
    type?: string
  }>(),
  {
    modelValue: '',
    placeholder: '',
    type: 'text',
  }
)

const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>()

const onInput = (event: Event) => {
  emit('update:modelValue', (event.target as HTMLInputElement).value)
}
</script>
