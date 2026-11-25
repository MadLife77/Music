<script setup lang="ts">
import Header from './components/header/index.vue'
import Aside from './components/aside/index.vue'
import Main from './components/main/index.vue'
import Footer from './components/footer/index.vue'
import DynamicBackground from '@/components/DynamicBackground/index.vue'
import { AudioPlayer } from '@/hooks/useAudioPlayer'

provide('audioPlayer', AudioPlayer())
</script>
<template>
  <DynamicBackground />
  <div class="absolute w-full flex flex-col h-full">
    <div
      class="w-full flex flex-col h-full overflow-hidden backdrop-blur-lg bg-white/30 dark:bg-gray-900/30"
    >
      <Header />
      <div class="flex flex-1 overflow-hidden">
        <Aside />
        <Main />
      </div>
      <Footer />
    </div>
  </div>
</template>
