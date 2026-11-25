<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from "vue";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { getAllSongs, getPlaylistSongIds, updatePlaylistSongs } from "@/api/system";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";

import Search from "@iconify-icons/ri/search-line";
import Refresh from "@iconify-icons/ep/refresh";
import Plus from "@iconify-icons/ri/add-line";

interface Song {
  songId: number;
  songName: string;
  singer: string;
  album: string;
  musicStyle: string;
  releaseDate: string;
  coverUrl: string;
}

const props = defineProps<{
  playlistId: number;
  playlistTitle: string;
}>();

const emit = defineEmits<{
  (e: "success"): void;
}>();

const loading = ref(false);
const selectedSongs = ref<Song[]>([]);
const songList = ref<Song[]>([]);
const existingSongIds = ref<number[]>([]); // 歌单中已有的歌曲ID
const pureTableRef = ref<any>(null);

const pagination = reactive({
  total: 0,
  pageSize: 10,
  currentPage: 1
});

const form = reactive({
  songName: "",
  singer: "",
  musicStyle: ""
});

// 曲风选项（与歌曲管理一致）
const musicStyleOptions = [
  { label: "节奏布鲁斯", value: "节奏布鲁斯" },
  { label: "欧美流行", value: "欧美流行" },
  { label: "华语流行", value: "华语流行" },
  { label: "粤语流行", value: "粤语流行" },
  { label: "国风流行", value: "国风流行" },
  { label: "韩语流行", value: "韩语流行" },
  { label: "日本流行", value: "日本流行" },
  { label: "嘻哈说唱", value: "嘻哈说唱" },
  { label: "非洲节拍", value: "非洲节拍" },
  { label: "原声带", value: "原声带" },
  { label: "轻音乐", value: "轻音乐" },
  { label: "摇滚", value: "摇滚" },
  { label: "朋克", value: "朋克" },
  { label: "电子", value: "电子" },
  { label: "国风", value: "国风" },
  { label: "乡村", value: "乡村" },
  { label: "古典", value: "古典" }
];

const columns = [
  {
    type: "selection",
    width: 55,
    fixed: "left"
  },
  {
    label: "封面",
    prop: "coverUrl",
    width: 70,
    slot: "cover",
    fixed: "left"
  },
  {
    label: "歌曲名",
    prop: "songName",
    minWidth: 120
  },
  {
    label: "歌手",
    prop: "singer",
    minWidth: 120
  },
  {
    label: "专辑",
    prop: "album",
    minWidth: 120
  },
  {
    label: "曲风",
    prop: "musicStyle",
    minWidth: 120,
    slot: "style"
  },
  {
    label: "发行日期",
    prop: "releaseDate",
    minWidth: 120
  }
];

// 获取歌单中已有的歌曲ID列表
const fetchExistingSongIds = async () => {
  try {
    const result = await getPlaylistSongIds(props.playlistId);
    if (result.code === 0 && result.data) {
      existingSongIds.value = result.data as number[];
    }
  } catch (error) {
    console.error("获取歌单已有歌曲失败:", error);
  }
};

// 自动勾选已有歌曲
const autoSelectExistingSongs = () => {
  if (!pureTableRef.value || !existingSongIds.value.length) return;
  
  const tableRef = pureTableRef.value.getTableRef?.() || pureTableRef.value;
  if (!tableRef) return;
  
  // 清除当前选择
  tableRef.clearSelection();
  
  // 勾选当前页中已有的歌曲
  songList.value.forEach((song) => {
    if (existingSongIds.value.includes(song.songId)) {
      tableRef.toggleRowSelection(song, true);
    }
  });
};

const onSearch = async () => {
  loading.value = true;
  try {
    const result = await getAllSongs({
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      songName: form.songName || null,
      singer: form.singer || null,
      musicStyle: form.musicStyle || null
    });

    if (result.code === 0 && result.data) {
      pagination.total = result.data.total || 0;
      songList.value = (result.data.items || []).map((item: any) => ({
        songId: item.songId,
        songName: item.songName,
        singer: item.singer,
        album: item.album,
        musicStyle: item.musicStyle,
        releaseDate: item.releaseDate,
        coverUrl: item.coverUrl
      }));
      
      // 数据加载完成后，自动勾选已有歌曲
      nextTick(() => {
        autoSelectExistingSongs();
      });
    }
  } catch (error) {
    console.error("获取歌曲列表失败:", error);
    message("获取歌曲列表失败", { type: "error" });
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  form.songName = "";
  form.singer = "";
  form.musicStyle = "";
  onSearch();
};

const handleSelectionChange = (selection: Song[]) => {
  selectedSongs.value = selection;
};

const handleSizeChange = (val: number) => {
  pagination.pageSize = val;
  onSearch();
};

const handleCurrentChange = (val: number) => {
  pagination.currentPage = val;
  onSearch();
};

const handleSave = async () => {
  // 计算需要添加和移除的歌曲
  const currentSelectedIds = selectedSongs.value.map(song => song.songId);
  const initialExistingIds = existingSongIds.value;
  
  // 需要添加的歌曲：当前选中但之前不存在的
  const addSongIds = currentSelectedIds.filter(id => !initialExistingIds.includes(id));
  // 需要移除的歌曲：之前存在但当前未选中的
  const removeSongIds = initialExistingIds.filter(id => !currentSelectedIds.includes(id));
  
  if (addSongIds.length === 0 && removeSongIds.length === 0) {
    message("没有变化，无需保存", { type: "info" });
    return;
  }

  try {
    const changeMessage = [];
    if (addSongIds.length > 0) {
      changeMessage.push(`添加 ${addSongIds.length} 首歌曲`);
    }
    if (removeSongIds.length > 0) {
      changeMessage.push(`移除 ${removeSongIds.length} 首歌曲`);
    }
    
    await ElMessageBox.confirm(
      `确定要${changeMessage.join("，")}到歌单"${props.playlistTitle}"吗？`,
      "确认修改",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    const result = await updatePlaylistSongs({
      playlistId: props.playlistId,
      addSongIds: addSongIds,
      removeSongIds: removeSongIds
    });

    if (result.code === 0) {
      message(result.message || "操作成功", { type: "success" });
      // 更新已有歌曲列表
      await fetchExistingSongIds();
      emit("success");
    } else {
      message("操作失败: " + result.message, { type: "error" });
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("操作失败:", error);
      message("操作失败", { type: "error" });
    }
  }
};

onMounted(async () => {
  await fetchExistingSongIds();
  onSearch();
});
</script>

<template>
  <div class="select-song-container">
    <el-form :inline="true" :model="form" class="search-form">
      <el-form-item label="歌曲名：" prop="songName">
        <el-input
          v-model="form.songName"
          placeholder="请输入歌曲名"
          clearable
          class="!w-[180px]"
        />
      </el-form-item>
      <el-form-item label="歌手：" prop="singer">
        <el-input
          v-model="form.singer"
          placeholder="请输入歌手"
          clearable
          class="!w-[180px]"
        />
      </el-form-item>
      <el-form-item label="曲风：" prop="musicStyle">
        <el-select
          v-model="form.musicStyle"
          placeholder="请选择"
          clearable
          class="!w-[180px]"
        >
          <el-option
            v-for="item in musicStyleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          :icon="useRenderIcon(Search)"
          :loading="loading"
          @click="onSearch"
        >
          搜索
        </el-button>
        <el-button :icon="useRenderIcon(Refresh)" @click="resetForm">
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <div class="table-container">
      <pure-table
        ref="pureTableRef"
        row-key="songId"
        :loading="loading"
        :data="songList"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @page-size-change="handleSizeChange"
        @page-current-change="handleCurrentChange"
      >
        <template #cover="{ row }">
          <el-image
            :src="row.coverUrl || '/song.jpg'"
            fit="cover"
            class="w-[50px] h-[50px] rounded"
          />
        </template>
        <template #style="{ row }">
          <el-tag v-if="row.musicStyle" size="small">{{ row.musicStyle }}</el-tag>
        </template>
      </pure-table>
    </div>

    <div class="footer-actions">
      <el-button type="primary" :icon="useRenderIcon(Plus)" @click="handleSave">
        保存修改 ({{ selectedSongs.length }} 首选中)
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.select-song-container {
  padding: 16px;
}

.search-form {
  margin-bottom: 16px;
}

.table-container {
  max-height: 400px;
  overflow-y: auto;
}

.footer-actions {
  margin-top: 16px;
  text-align: right;
}
</style>
