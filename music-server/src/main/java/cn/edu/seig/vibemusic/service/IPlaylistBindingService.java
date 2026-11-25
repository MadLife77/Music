package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.dto.AddSongToPlaylistDTO;
import cn.edu.seig.vibemusic.model.dto.UpdatePlaylistSongsDTO;
import cn.edu.seig.vibemusic.model.entity.PlaylistBinding;
import cn.edu.seig.vibemusic.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 */
public interface IPlaylistBindingService extends IService<PlaylistBinding> {

    /**
     * 批量添加歌曲到歌单
     *
     * @param addSongToPlaylistDTO 添加歌曲到歌单信息
     * @return 结果
     */
    Result addSongsToPlaylist(AddSongToPlaylistDTO addSongToPlaylistDTO);

    /**
     * 获取歌单中已有的歌曲ID列表
     *
     * @param playlistId 歌单ID
     * @return 歌曲ID列表
     */
    List<Long> getExistingSongIds(Long playlistId);

    /**
     * 更新歌单歌曲（添加和移除）
     *
     * @param updatePlaylistSongsDTO 更新歌单歌曲信息
     * @return 结果
     */
    Result updatePlaylistSongs(UpdatePlaylistSongsDTO updatePlaylistSongsDTO);

}
