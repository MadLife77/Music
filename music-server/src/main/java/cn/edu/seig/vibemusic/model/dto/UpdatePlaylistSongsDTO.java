package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 更新歌单歌曲DTO（用于新增和移除歌曲）
 */
@Data
public class UpdatePlaylistSongsDTO {

    /**
     * 歌单ID
     */
    private Long playlistId;

    /**
     * 要添加的歌曲ID列表
     */
    private List<Long> addSongIds;

    /**
     * 要移除的歌曲ID列表
     */
    private List<Long> removeSongIds;
}
