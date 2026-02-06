<template>
  <div class="flex items-center gap-2 mb-4">
    <span class="text-sm text-slate-400">配置名称</span>
    <div class="flex items-center gap-1 bg-slate-800 rounded-lg p-1">
      <button
        v-for="config in configs"
        :key="config.id"
        :class="[
          'px-3 py-1.5 text-sm rounded-md transition-colors flex items-center gap-2',
          activeConfigId === config.id
            ? 'bg-blue-600 text-white'
            : 'text-slate-400 hover:text-slate-200 hover:bg-slate-700'
        ]"
        @click="handleSelect(config.id)"
      >
        {{ config.name }}
        <button
          v-if="configs.length > 1"
          class="w-4 h-4 rounded-full hover:bg-red-500/20 hover:text-red-400 flex items-center justify-center"
          @click.stop="handleRemove(config.id)"
        >
          <X class="w-3 h-3" />
        </button>
      </button>
      <button
        class="px-3 py-1.5 text-sm rounded-md text-slate-400 hover:text-slate-200 hover:bg-slate-700 transition-colors flex items-center gap-1"
        @click="handleAdd"
      >
        <Plus class="w-4 h-4" />
        新增
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, X } from 'lucide-vue-next'

export interface ConfigTab {
  id: string
  name: string
}

interface Props {
  configs: ConfigTab[]
  activeConfigId: string | null
}

interface Emits {
  (e: 'select', configId: string): void
  (e: 'add'): void
  (e: 'remove', configId: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const handleSelect = (configId: string) => {
  emit('select', configId)
}

const handleAdd = () => {
  emit('add')
}

const handleRemove = (configId: string) => {
  emit('remove', configId)
}
</script>
