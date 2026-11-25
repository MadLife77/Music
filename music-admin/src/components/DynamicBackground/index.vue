<script setup lang="ts">
// 动态背景组件 - 支持亮色和暗色模式
</script>

<template>
  <div class="dynamic-bg">
    <!-- 浮动渐变球 -->
    <div class="gradient-orb orb-1"></div>
    <div class="gradient-orb orb-2"></div>
    <div class="gradient-orb orb-3"></div>
    <div class="gradient-orb orb-4"></div>
    <!-- 网格叠加层 -->
    <div class="grid-overlay"></div>
  </div>
</template>

<style scoped>
.dynamic-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
  background: linear-gradient(135deg, 
    hsla(210, 100%, 97%, 1) 0%, 
    hsla(280, 50%, 97%, 1) 50%,
    hsla(340, 50%, 97%, 1) 100%
  );
  transition: background 0.5s ease;
}

/* 暗色模式背景 - 使用 html.dark 选择器 */
:global(html.dark) .dynamic-bg {
  background: linear-gradient(135deg, 
    hsla(222, 47%, 8%, 1) 0%, 
    hsla(260, 47%, 12%, 1) 50%,
    hsla(340, 47%, 10%, 1) 100%
  );
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s infinite ease-in-out;
}

/* 亮色模式渐变球颜色 */
.orb-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -200px;
  left: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  top: 20%;
  right: -150px;
  animation-delay: -5s;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  bottom: -100px;
  left: 30%;
  animation-delay: -10s;
}

.orb-4 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  top: 40%;
  left: 20%;
  animation-delay: -15s;
}

/* 暗色模式渐变球颜色 - 更深的色调 */
:global(html.dark) .orb-1 {
  background: linear-gradient(135deg, #1a1f6d 0%, #2d1b4e 100%);
  opacity: 0.6;
}

:global(html.dark) .orb-2 {
  background: linear-gradient(135deg, #4a1f5c 0%, #5c1f2e 100%);
  opacity: 0.6;
}

:global(html.dark) .orb-3 {
  background: linear-gradient(135deg, #0f3460 0%, #0d4f4f 100%);
  opacity: 0.6;
}

:global(html.dark) .orb-4 {
  background: linear-gradient(135deg, #0f5132 0%, #0d4a3a 100%);
  opacity: 0.6;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(50px, -30px) scale(1.1);
  }
  50% {
    transform: translate(-30px, 50px) scale(0.9);
  }
  75% {
    transform: translate(-50px, -20px) scale(1.05);
  }
}

/* 网格叠加层 */
.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 30s linear infinite;
}

:global(html.dark) .grid-overlay {
  background-image: 
    linear-gradient(rgba(99, 102, 241, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.05) 1px, transparent 1px);
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}
</style>