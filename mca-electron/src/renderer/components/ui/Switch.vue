<template>
  <label class="inline-flex items-center cursor-pointer">
    <input
      type="checkbox"
      class="sr-only"
      :checked="modelValue"
      @change="onChange"
    />
    <span
      :class="[
        'h-5 w-9 rounded-full border border-slate-700 transition',
        modelValue ? 'bg-blue-600' : 'bg-slate-800',
      ]"
    >
      <span
        :class="[
          'block h-4 w-4 translate-x-0.5 rounded-full bg-white transition',
          modelValue ? 'translate-x-4' : 'translate-x-0.5',
        ]"
      ></span>
    </span>
  </label>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{ modelValue?: boolean }>(), {
  modelValue: false,
})

const emit = defineEmits<{ (event: 'update:modelValue', value: boolean): void }>()

const onChange = (event: Event) => {
  emit('update:modelValue', (event.target as HTMLInputElement).checked)
}
</script>
