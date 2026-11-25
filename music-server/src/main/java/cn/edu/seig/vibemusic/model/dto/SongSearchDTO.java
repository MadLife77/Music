package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

/**
 * 歌曲搜索DTO
 */
@Data
public class SongSearchDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String songName;
    private String singer;
    private String musicStyle;
}
