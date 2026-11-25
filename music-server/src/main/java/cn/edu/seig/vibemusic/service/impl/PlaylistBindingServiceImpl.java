package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.constant.MessageConstant;
import cn.edu.seig.vibemusic.model.dto.AddSongToPlaylistDTO;
import cn.edu.seig.vibemusic.model.dto.UpdatePlaylistSongsDTO;
import cn.edu.seig.vibemusic.model.entity.PlaylistBinding;
import cn.edu.seig.vibemusic.mapper.PlaylistBindingMapper;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.IPlaylistBindingService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class PlaylistBindingServiceImpl extends ServiceImpl<PlaylistBindingMapper, PlaylistBinding> implements IPlaylistBindingService {

    /**
     * 批量添加歌曲到歌单
     *
     * @param addSongToPlaylistDTO 添加歌曲到歌单信息
     * @return 结果
     */
    @Override
    @Transactional
    public Result addSongsToPlaylist(AddSongToPlaylistDTO addSongToPlaylistDTO) {
        Long playlistId = addSongToPlaylistDTO.getPlaylistId();
        List<Long> songIds = addSongToPlaylistDTO.getSongIds();

        if (playlistId == null || songIds == null || songIds.isEmpty()) {
            return Result.error("参数" + MessageConstant.INVALID);
        }

        // 查询该歌单已有的歌曲，避免重复添加
        List<PlaylistBinding> existingBindings = this.list(
                new QueryWrapper<PlaylistBinding>().eq("playlist_id", playlistId)
        );
        List<Long> existingSongIds = existingBindings.stream()
                .map(PlaylistBinding::getSongId)
                .toList();

        int successCount = 0;
        int duplicateCount = 0;

        for (Long songId : songIds) {
            // 跳过已存在的歌曲
            if (existingSongIds.contains(songId)) {
                duplicateCount++;
                continue;
            }

            PlaylistBinding binding = new PlaylistBinding();
            binding.setPlaylistId(playlistId);
            binding.setSongId(songId);

            if (this.save(binding)) {
                successCount++;
            }
        }

        String message = String.format("成功添加 %d 首歌曲", successCount);
        if (duplicateCount > 0) {
            message += String.format("，%d 首歌曲已存在于歌单中", duplicateCount);
        }

        return Result.success(message);
    }

    /**
     * 获取歌单中已有的歌曲ID列表
     *
     * @param playlistId 歌单ID
     * @return 歌曲ID列表
     */
    @Override
    public List<Long> getExistingSongIds(Long playlistId) {
        List<PlaylistBinding> existingBindings = this.list(
                new QueryWrapper<PlaylistBinding>().eq("playlist_id", playlistId)
        );
        return existingBindings.stream()
                .map(PlaylistBinding::getSongId)
                .toList();
    }

    /**
     * 更新歌单歌曲（添加和移除）
     *
     * @param updatePlaylistSongsDTO 更新歌单歌曲信息
     * @return 结果
     */
    @Transactional
    public Result updatePlaylistSongs(UpdatePlaylistSongsDTO updatePlaylistSongsDTO) {
        Long playlistId = updatePlaylistSongsDTO.getPlaylistId();
        List<Long> addSongIds = updatePlaylistSongsDTO.getAddSongIds();
        List<Long> removeSongIds = updatePlaylistSongsDTO.getRemoveSongIds();

        if (playlistId == null) {
            return Result.error("参数" + MessageConstant.INVALID);
        }

        int addCount = 0;
        int removeCount = 0;

        // 处理移除歌曲
        if (removeSongIds != null && !removeSongIds.isEmpty()) {
            for (Long songId : removeSongIds) {
                boolean removed = this.remove(
                        new QueryWrapper<PlaylistBinding>()
                                .eq("playlist_id", playlistId)
                                .eq("song_id", songId)
                );
                if (removed) {
                    removeCount++;
                }
            }
        }

        // 处理添加歌曲（跳过已存在的）
        if (addSongIds != null && !addSongIds.isEmpty()) {
            List<Long> existingSongIds = getExistingSongIds(playlistId);
            for (Long songId : addSongIds) {
                if (!existingSongIds.contains(songId)) {
                    PlaylistBinding binding = new PlaylistBinding();
                    binding.setPlaylistId(playlistId);
                    binding.setSongId(songId);
                    if (this.save(binding)) {
                        addCount++;
                    }
                }
            }
        }

        String message = "";
        if (addCount > 0) {
            message += String.format("成功添加 %d 首歌曲", addCount);
        }
        if (removeCount > 0) {
            if (!message.isEmpty()) {
                message += "，";
            }
            message += String.format("成功移除 %d 首歌曲", removeCount);
        }
        if (message.isEmpty()) {
            message = "没有变化";
        }

        return Result.success(message);
    }

}
