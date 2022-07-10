package otaku.info.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 類似別名人物を削除するためのメンバーデータ
 */
@Getter
public enum MemberElimEnum {
    SATOSHI_OHNO(1L, 70L, "大野智敬"),
    KEN_MIYAKE(2L, 68L, "三宅健太");

    private final Long id;
    private final Long memberId;
    private final String elimName;

    MemberElimEnum (Long id, Long memberId, String elimName) {
        this.id = id;
        this.memberId = memberId;
        this.elimName = elimName;
    }

    /**
     * 引数IDのメンバーの類似別人データがあるか判定
     *
     * @param memberId
     * @return
     */
    public static boolean hasElimData (Long memberId) {
        return Arrays.stream(MemberElimEnum.values()).anyMatch(e -> e.memberId.equals(memberId));
    }

    /**
     * 引数memberIdのelimNameリストを戻す
     *
     * @param memberId
     * @return
     */
    public static List<String> getElimNameList (Long memberId) {
        return Arrays.stream(MemberElimEnum.values()).filter(e -> e.memberId.equals(memberId)).map(e -> e.elimName).collect(Collectors.toList());
    }

}
