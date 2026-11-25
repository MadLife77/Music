<script setup lang="tsx">
import { ref, watch, onMounted } from "vue";
import ReCropper from "@/components/ReCropper";
import { formatBytes } from "@pureadmin/utils";
import defaultAvatar from "@/assets/user.jpg";

defineOptions({
  name: "ReCropperPreview"
});

const props = defineProps({
  imgSrc: String
});

// 兜底头像：当原始头像 URL（例如 MinIO 中已丢失的对象、或陈旧缓存里的坏链）
// 加载失败时，改用本地默认图片，避免 cropperjs 的 ready 事件永不触发
// 导致弹窗一直停留在 loading、无法裁剪/上传（无法用上传功能自修复）
const safeSrc = ref<string>(props.imgSrc || defaultAvatar);

function probe(src: string): Promise<boolean> {
  return new Promise(resolve => {
    if (!src) return resolve(false);
    let settled = false;
    const finish = (ok: boolean) => {
      if (settled) return;
      settled = true;
      resolve(ok);
    };
    const img = new Image();
    img.onload = () => finish(true);
    img.onerror = () => finish(false);
    // 兜底：3 秒内既没成功也没失败，按加载失败处理，避免 loading 永久卡住
    setTimeout(() => finish(false), 3000);
    img.src = src;
  });
}

async function resolveSrc() {
  const src = props.imgSrc;
  if (!src) {
    safeSrc.value = defaultAvatar;
    return;
  }
  const ok = await probe(src);
  safeSrc.value = ok ? src : defaultAvatar;
}

watch(() => props.imgSrc, resolveSrc, { immediate: true });
onMounted(resolveSrc);

const emit = defineEmits(["cropper"]);

const infos = ref();
const popoverRef = ref();
const refCropper = ref();
const showPopover = ref(false);
const cropperImg = ref<string>("");

function onCropper({ base64, blob, info }) {
  infos.value = info;
  cropperImg.value = base64;
  emit("cropper", { base64, blob, info });
}

function hidePopover() {
  popoverRef.value.hide();
}

defineExpose({ hidePopover });
</script>

<template>
  <div v-loading="!showPopover" element-loading-background="transparent">
    <el-popover
      ref="popoverRef"
      :visible="showPopover"
      placement="right"
      width="18vw"
    >
      <template #reference>
        <div class="w-[18vw]">
          <ReCropper
            :key="safeSrc"
            ref="refCropper"
            :src="safeSrc"
            circled
            @cropper="onCropper"
            @readied="showPopover = true"
            @error="showPopover = true"
          />
          <p v-show="showPopover" class="mt-1 text-center">
            温馨提示：右键上方裁剪区可开启功能菜单
          </p>
        </div>
      </template>
      <div class="flex flex-wrap justify-center items-center text-center">
        <el-image
          v-if="cropperImg"
          :src="cropperImg"
          :preview-src-list="Array.of(cropperImg)"
          fit="cover"
        />
        <div v-if="infos" class="mt-1">
          <p>
            图像大小：{{ parseInt(infos.width) }} ×
            {{ parseInt(infos.height) }}像素
          </p>
          <p>
            文件大小：{{ formatBytes(infos.size) }}（{{ infos.size }} 字节）
          </p>
        </div>
      </div>
    </el-popover>
  </div>
</template>
