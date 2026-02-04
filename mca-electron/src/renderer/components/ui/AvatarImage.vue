<template>
  <img :src="src" :alt="alt" class="h-full w-full object-cover" />
</template>

<script setup lang="ts">
withDefaults(defineProps<{ src?: string; alt?: string }>(), {
  src: '',
  alt: '',
})
</script>
