package otaku.info.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * WordPressタグのEnum
 *
 */
@Getter
public enum WpTagEnum {

    ABCZ(19, 14),
    ARASHI(16, 11),
    HEYSAYJUMP(21, 19),
    JOHNNYSWEST(20, 15),
    KANJANI8(13, 7),
    KATTUN(22, 20),
    KINGANDPRINCE(12, 16),
    KINKIKIDS(23, 21),
    KISMYFT2(18, 13),
    NANIWADANSHI(10, 18),
    NEWS(17, 12),
    SEXYZONE(11, 8),
    SIXTONES(9, 17),
    SNOWMAN(8, 6),
    TOKIO(14, 9),
    V6(15, 10);

    private Integer wpTagId;
    private Integer dbTeamId;


    private WpTagEnum(int wpTagId, int dbTeamId) {
        this.wpTagId = wpTagId;
        this.dbTeamId = dbTeamId;
    }

    public static Integer getByDbTeamId(String teamId) {
        int dbTeamId = Integer.parseInt(teamId);
        WpTagEnum elem = Arrays.stream(WpTagEnum.values())
                .filter(e -> e.getDbTeamId().equals(dbTeamId))
                .findFirst()
                .orElse(null);
        return elem == null ? 0 : elem.getWpTagId();
    }
}
