package otaku.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WpDto {

    public String title;

    public String content;

    public String excerpt;

    public Integer[] tags;

    /** 投稿するブログパス */
    public String path;
}
