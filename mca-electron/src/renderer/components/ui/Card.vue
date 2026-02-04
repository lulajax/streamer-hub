<template>
  <div :class="['rounded-lg border border-slate-800 bg-slate-900 text-slate-100', $attrs.class]">
    <slot />
  </div>
</template>

<script setup lang="ts"></script>
