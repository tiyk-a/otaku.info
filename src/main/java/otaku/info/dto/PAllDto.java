package otaku.info.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * フロントでTV関連データを扱うDTO
 *
 */
@Setter
@Getter
public class PAllDto {

    // 確認前のprogramリスト
    private List<PDto> p;

    // 確認済みのPMリスト
    private List<PMDto> pm;
}
