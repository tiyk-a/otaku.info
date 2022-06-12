package otaku.info.dto;

import lombok.Getter;
import lombok.Setter;
import otaku.info.entity.ErrorJson;

import java.math.BigInteger;
import java.util.Map;
import java.util.List;

/**
 * トップ画面用のDTO
 * チーム個別画面とは異なり、各アイテムがチームの情報を持ちます
 *
 */
@Setter
@Getter
public class FAllDto {

    // IMがないItemを詰める
    private List<ItemTeamDto> i;

    // IMを集める
    private List<FIMDto> im;

    private List<ErrorJson> errJ;

    // 各チームの未チェックID件数
    private Map<BigInteger, BigInteger> itemNumberMap;
}
