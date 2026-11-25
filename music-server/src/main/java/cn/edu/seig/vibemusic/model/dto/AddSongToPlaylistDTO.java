package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 添加歌曲到歌单DTO
 */
@Data
public class AddSongToPlaylistDTO {
    private Long playlistId;
    private List<Long> songIds;
}
