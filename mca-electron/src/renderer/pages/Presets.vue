<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white">{{ t('presets') }}</h1>
        <p class="text-slate-400">{{ t('presetSubtitle') }}</p>
      </div>
      <Button class="bg-blue-600 hover:bg-blue-700" @click="startCreatePreset">
        {{ t('addPreset') }}
      </Button>
    </div>

    <Tabs v-model="activeTab" class="w-full">
      <TabsList class="bg-slate-900 border border-slate-800">
        <TabsTrigger value="presets">{{ t('presets') }}</TabsTrigger>
        <TabsTrigger value="anchors">{{ t('anchorLibrary') }}</TabsTrigger>
      </TabsList>

      <TabsContent value="presets" class="space-y-4">
        <div class="grid grid-cols-3 gap-6">
          <Card class="bg-slate-900 border-slate-800 col-span-1">
            <CardHeader>
              <CardTitle class="text-white">{{ t('presets') }}</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="space-y-2">
                <button
                  v-for="preset in store.presets"
                  :key="preset.id"
                  class="w-full text-left p-3 rounded-lg transition-colors border border-transparent"
                  :class="preset.id === selectedPresetId
                    ? 'bg-blue-500/20 border-blue-500/40'
                    : 'bg-slate-800/60 hover:bg-slate-800'"
                  @click="selectPreset(preset.id)"
                >
                  <div class="flex items-center justify-between">
                    <div>
                      <p class="text-white font-medium">{{ preset.name }}</p>
                      <p class="text-slate-400 text-xs">{{ formatMode(preset.mode) }}</p>
                    </div>
                    <Badge
                      v-if="preset.isDefault"
                      variant="secondary"
                      class="bg-amber-500/20 text-amber-300"
                    >
                      {{ t('default') }}
                    </Badge>
                  </div>
                </button>

                <div v-if="store.presets.length === 0" class="text-center text-slate-500 py-8">
                  <p>{{ t('noPresets') }}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card class="bg-slate-900 border-slate-800 col-span-2">
            <CardHeader>
              <CardTitle class="text-white">
                {{ isCreating ? t('createPreset') : t('presetDetails') }}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div v-if="isCreating" class="space-y-4">
                <div>
                  <label class="text-sm font-medium text-slate-300 mb-2 block">
                    {{ t('presetName') }}
                  </label>
                  <Input v-model="createForm.name" class="bg-slate-900 border-slate-700 text-white" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-300 mb-2 block">
                    {{ t('gameMode') }}
                  </label>
                  <Select v-model="createForm.gameMode">
                    <SelectTrigger class="bg-slate-800 border-slate-700 text-white">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="STICKER">{{ t('stickerMode') }}</SelectItem>
                      <SelectItem value="PK">{{ t('pkMode') }}</SelectItem>
                      <SelectItem value="FREE">{{ t('freeMode') }}</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div class="flex gap-3">
                  <Button class="bg-blue-600 hover:bg-blue-700" @click="handleCreatePreset">
                    {{ t('save') }}
                  </Button>
                  <Button variant="outline" class="border-slate-700 text-slate-300" @click="cancelCreate">
                    {{ t('cancel') }}
                  </Button>
                </div>
              </div>

              <div v-else-if="selectedPreset" class="space-y-6">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="text-sm font-medium text-slate-300 mb-2 block">
                      {{ t('presetName') }}
                    </label>
                    <Input v-model="editForm.name" class="bg-slate-900 border-slate-700 text-white" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-300 mb-2 block">
                      {{ t('gameMode') }}
                    </label>
                    <Select v-model="editForm.gameMode">
                      <SelectTrigger class="bg-slate-800 border-slate-700 text-white">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="STICKER">{{ t('stickerMode') }}</SelectItem>
                        <SelectItem value="PK">{{ t('pkMode') }}</SelectItem>
                        <SelectItem value="FREE">{{ t('freeMode') }}</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div class="flex items-center gap-3">
                  <Button class="bg-blue-600 hover:bg-blue-700" @click="handleUpdatePreset">
                    {{ t('save') }}
                  </Button>
                  <Button
                    variant="outline"
                    class="border-slate-700 text-slate-300"
                    :disabled="selectedPreset.isDefault"
                    @click="handleSetDefault(selectedPreset.id)"
                  >
                    {{ t('setDefault') }}
                  </Button>
                  <Button
                    variant="outline"
                    class="border-red-500/40 text-red-400 hover:text-red-300"
                    @click="handleDeletePreset(selectedPreset.id)"
                  >
                    {{ t('delete') }}
                  </Button>
                </div>

                <div class="border-t border-slate-800 pt-4 space-y-3">
                  <div class="flex items-center justify-between">
                    <div>
                      <p class="text-white font-medium">{{ t('linkedAnchors') }}</p>
                      <p class="text-slate-400 text-sm">{{ t('linkAnchorsHint') }}</p>
                    </div>
                  </div>

                  <div class="space-y-2">
                    <div
                      v-for="anchor in store.userAnchors"
                      :key="anchor.id"
                      class="flex items-center justify-between p-3 rounded-lg bg-slate-800/60"
                    >
                      <div>
                        <p class="text-white font-medium">{{ anchor.name }}</p>
                        <p class="text-slate-400 text-xs">{{ anchor.tiktokId || '-' }}</p>
                      </div>
                      <Switch
                        :modelValue="isAnchorLinked(anchor.id)"
                        @update:modelValue="(value) => handleToggleAnchor(anchor.id, value)"
                      />
                    </div>

                    <div v-if="store.userAnchors.length === 0" class="text-slate-500 text-center py-6">
                      {{ t('noAnchors') }}
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="text-slate-500 text-center py-10">
                {{ t('selectPresetHint') }}
              </div>
            </CardContent>
          </Card>
        </div>
      </TabsContent>

      <TabsContent value="anchors" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">{{ t('anchorLibrary') }}</CardTitle>
          </CardHeader>
          <CardContent class="space-y-6">
            <div class="grid grid-cols-3 gap-4">
              <div>
                <label class="text-sm font-medium text-slate-300 mb-2 block">{{ t('anchorName') }}</label>
                <Input v-model="newAnchor.name" class="bg-slate-900 border-slate-700 text-white" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-300 mb-2 block">{{ t('tiktokId') }}</label>
                <Input v-model="newAnchor.tiktokId" class="bg-slate-900 border-slate-700 text-white" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-300 mb-2 block">{{ t('avatarUrl') }}</label>
                <Input v-model="newAnchor.avatarUrl" class="bg-slate-900 border-slate-700 text-white" />
              </div>
            </div>
            <Button class="bg-blue-600 hover:bg-blue-700" @click="handleCreateAnchor">
              {{ t('addAnchor') }}
            </Button>

            <div class="space-y-3">
              <div
                v-for="anchor in store.userAnchors"
                :key="anchor.id"
                class="p-4 rounded-lg bg-slate-800/60 space-y-3"
              >
                <div class="grid grid-cols-3 gap-4">
                  <Input v-model="anchor.name" class="bg-slate-900 border-slate-700 text-white" />
                  <Input v-model="anchor.tiktokId" class="bg-slate-900 border-slate-700 text-white" />
                  <Input v-model="anchor.avatarUrl" class="bg-slate-900 border-slate-700 text-white" />
                </div>
                <div class="flex gap-3">
                  <Button variant="outline" class="border-slate-700 text-slate-300" @click="handleUpdateAnchor(anchor)">
                    {{ t('save') }}
                  </Button>
                  <Button
                    variant="outline"
                    class="border-red-500/40 text-red-400 hover:text-red-300"
                    @click="handleDeleteAnchor(anchor.id)"
                  >
                    {{ t('delete') }}
                  </Button>
                </div>
              </div>

              <div v-if="store.userAnchors.length === 0" class="text-center text-slate-500 py-8">
                {{ t('noAnchors') }}
              </div>
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { createUserAnchor, deleteUserAnchor, updateUserAnchor } from '@/lib/anchorsApi'
import {
  attachAnchorToPreset,
  createPreset,
  deletePreset,
  detachAnchorFromPreset,
  setDefaultPreset,
  updatePreset,
} from '@/lib/presetsApi'
import { mapApiPreset, mapApiUserAnchor } from '@/lib/mappers'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import Input from '@/components/ui/Input.vue'
import Tabs from '@/components/ui/Tabs.vue'
import TabsContent from '@/components/ui/TabsContent.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'
import Select from '@/components/ui/Select.vue'
import SelectContent from '@/components/ui/SelectContent.vue'
import SelectItem from '@/components/ui/SelectItem.vue'
import SelectTrigger from '@/components/ui/SelectTrigger.vue'
import SelectValue from '@/components/ui/SelectValue.vue'
import Switch from '@/components/ui/Switch.vue'
import type { Preset, UserAnchor } from '@/types'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const activeTab = ref('presets')
const selectedPresetId = ref<string | null>(store.currentPreset?.id ?? null)
const isCreating = ref(false)

const createForm = ref({
  name: '',
  gameMode: 'STICKER' as 'STICKER' | 'PK' | 'FREE',
})

const editForm = ref({
  name: '',
  gameMode: 'STICKER' as 'STICKER' | 'PK' | 'FREE',
})

const newAnchor = ref({
  name: '',
  tiktokId: '',
  avatarUrl: '',
})

const selectedPreset = computed<Preset | null>(() => {
  if (!selectedPresetId.value) return null
  return store.presets.find((preset) => preset.id === selectedPresetId.value) ?? null
})

watch(
  () => store.currentPreset?.id,
  (value) => {
    if (value) {
      selectedPresetId.value = value
    }
  }
)

watch(
  () => selectedPreset.value,
  (preset) => {
    if (preset) {
      editForm.value = {
        name: preset.name,
        gameMode: preset.mode === 'pk' ? 'PK' : preset.mode === 'free' ? 'FREE' : 'STICKER',
      }
    }
  },
  { immediate: true }
)

const formatMode = (mode: Preset['mode']) => {
  switch (mode) {
    case 'pk':
      return t('pkMode')
    case 'free':
      return t('freeMode')
    default:
      return t('stickerMode')
  }
}

const selectPreset = (presetId: string) => {
  selectedPresetId.value = presetId
  const preset = store.presets.find((item) => item.id === presetId) ?? null
  if (preset) {
    store.selectPresetForMode(preset.mode, preset)
  } else {
    store.setCurrentPreset(null)
  }
  isCreating.value = false
}

const startCreatePreset = () => {
  isCreating.value = true
  createForm.value = { name: '', gameMode: 'STICKER' }
}

const cancelCreate = () => {
  isCreating.value = false
}

const upsertPreset = (preset: Preset) => {
  const exists = store.presets.some((item) => item.id === preset.id)
  const next = exists
    ? store.presets.map((item) => (item.id === preset.id ? preset : item))
    : [...store.presets, preset]
  store.setPresets(next)
  store.selectPresetForMode(preset.mode, preset)
  selectedPresetId.value = preset.id
}

const handleCreatePreset = async () => {
  if (!createForm.value.name.trim()) {
    toast({
      title: t('error'),
      description: t('presetNameRequired'),
      variant: 'destructive',
    })
    return
  }

  const response = await createPreset({
    name: createForm.value.name.trim(),
    gameMode: createForm.value.gameMode,
  })

  if (response?.success && response.data) {
    const preset = mapApiPreset(response.data)
    upsertPreset(preset)
    isCreating.value = false
    toast({ title: t('success'), description: t('presetCreated') })
  } else {
    toast({
      title: t('error'),
      description: response?.error || t('networkError'),
      variant: 'destructive',
    })
  }
}

const handleUpdatePreset = async () => {
  if (!selectedPreset.value) return
  if (!editForm.value.name.trim()) {
    toast({
      title: t('error'),
      description: t('presetNameRequired'),
      variant: 'destructive',
    })
    return
  }
  const response = await updatePreset(selectedPreset.value.id, {
    name: editForm.value.name.trim(),
    gameMode: editForm.value.gameMode,
  })
  if (response?.success && response.data) {
    upsertPreset(mapApiPreset(response.data))
    toast({ title: t('success'), description: t('presetUpdated') })
  } else {
    toast({
      title: t('error'),
      description: response?.error || t('networkError'),
      variant: 'destructive',
    })
  }
}

const handleDeletePreset = async (presetId: string) => {
  const response = await deletePreset(presetId)
  if (response?.success) {
    const next = store.presets.filter((item) => item.id !== presetId)
    store.setPresets(next)
    const nextPreset = next[0] ?? null
    store.setCurrentPreset(nextPreset)
    selectedPresetId.value = nextPreset?.id ?? null
    toast({ title: t('success'), description: t('presetDeleted') })
  } else {
    toast({
      title: t('error'),
      description: response?.error || t('networkError'),
      variant: 'destructive',
    })
  }
}

const handleSetDefault = async (presetId: string) => {
  const response = await setDefaultPreset(presetId)
  if (response?.success && response.data) {
    const next = store.presets.map((item) => ({
      ...item,
      isDefault: item.id === presetId,
    }))
    store.setPresets(next)
    const updated = mapApiPreset(response.data)
    upsertPreset({ ...updated, isDefault: true })
    toast({ title: t('success'), description: t('defaultUpdated') })
  } else {
    toast({
      title: t('error'),
      description: response?.error || t('networkError'),
      variant: 'destructive',
    })
  }
}

const isAnchorLinked = (anchorId: string) =>
  selectedPreset.value?.anchors.some((anchor) => anchor.id === anchorId) ?? false

const handleToggleAnchor = async (anchorId: string, value: boolean) => {
  if (!selectedPreset.value) return
  if (value) {
    const response = await attachAnchorToPreset(selectedPreset.value.id, {
      anchorId,
      displayOrder: selectedPreset.value.anchors.length,
    })
    if (response?.success && response.data) {
      upsertPreset(mapApiPreset(response.data))
    } else {
      toast({
        title: t('error'),
        description: response?.error || t('networkError'),
        variant: 'destructive',
      })
    }
  } else {
    const response = await detachAnchorFromPreset(selectedPreset.value.id, anchorId)
    if (response?.success && response.data) {
      upsertPreset(mapApiPreset(response.data))
    } else {
      toast({
        title: t('error'),
        description: response?.error || t('networkError'),
        variant: 'destructive',
      })
    }
  }
}

const handleCreateAnchor = async () => {
  if (!newAnchor.value.name.trim()) {
    toast({
      title: t('error'),
      description: t('anchorNameRequired'),
      variant: 'destructive',
    })
    return
  }

  const response = await createUserAnchor({
    name: newAnchor.value.name.trim(),
    tiktokId: newAnchor.value.tiktokId.trim() || undefined,
    avatarUrl: newAnchor.value.avatarUrl.trim() || undefined,
  })

  if (response?.success && response.data) {
    const anchor = mapApiUserAnchor(response.data) as UserAnchor
    store.addUserAnchor(anchor)
    newAnchor.value = { name: '', tiktokId: '', avatarUrl: '' }
    toast({ title: t('success'), description: t('anchorCreated') })
  } else {
    toast({
      title: t('error'),
      description: response?.error || t('networkError'),
      variant: 'destructive',
    })
  }
}

const handleUpdateAnchor = async (anchor: UserAnchor) => {
  if (!anchor.name?.trim()) {
    toast({
      title: t('error'),
      description: t('anchorNameRequired'),
      variant: 'destructive',
    })
    return
  }
  const response = await updateUserAnchor(anchor.id, {
    name: anchor.name?.trim() || '',
    tiktokId: anchor.tiktokId?.trim() ?? '',
    avatarUrl: anchor.avatarUrl?.trim() ?? '',
  })

  if (response?.success && response.data) {
    store.updateUserAnchor(anchor.id, mapApiUserAnchor(response.data))
    toast({ title: t('success'), description: t('anchorUpdated') })
  } else {
    toast({
      title: t('error'),
      description: response?.error || t('networkError'),
      variant: 'destructive',
    })
  }
}

const handleDeleteAnchor = async (anchorId: string) => {
  const response = await deleteUserAnchor(anchorId)
  if (response?.success) {
    store.removeUserAnchor(anchorId)
    toast({ title: t('success'), description: t('anchorDeleted') })
  } else {
    toast({
      title: t('error'),
      description: response?.error || t('anchorDeleteBlocked'),
      variant: 'destructive',
    })
  }
}
</script>
