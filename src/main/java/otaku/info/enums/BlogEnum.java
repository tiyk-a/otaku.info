package otaku.info.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BLOGの情報
 *
 */
@Getter
public enum BlogEnum {

    MAIN(7L, "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV", 4388L, 1707L, 176L, "https://otakuinfo.fun/wp-content/uploads/2022/03/schedule.png"),
    SIXTONES(17L, "https://sixtones.otakuinfo.fun/", "sixtones:r9Ux DkUr 4cqp or4q FX3c U1sM", 20L, 22L, 52L, "https://sixtones.otakuinfo.fun/wp-content/uploads/2022/03/schedule4.png"),
    SNOWMAN(6L, "https://snowman.otakuinfo.fun/", "snowman:k9UR wNsr LzdK IYFT oQ4s gKuJ", 18L, 20L, 39L, "https://snowman.otakuinfo.fun/wp-content/uploads/2022/03/schedule3.png"),
    KINGANDPRINCE(16L, "https://kingandprince.otakuinfo.fun/", "king:5Hsj xJot J6Ez jkA1 ZXse ELwX", 18L, 20L, 35L, "https://kingandprince.otakuinfo.fun/wp-content/uploads/2022/03/schedule2.png"),
    NANIWADANSHI(18L, "https://naniwadanshi.otakuinfo.fun/", "naniwa:0iqK j9dg a2Ec aQ0h gJOI v0rs", 18L, 20L, 27L, "https://naniwadanshi.otakuinfo.fun/wp-content/uploads/2022/03/schedule1.png");

    /** IDはteamIdに繋がる */
    private final Long id;

    /** 固有WPブログが用意されてる場合、サブドメインの固有値部分を指定。ない場合、null */
    private final String subDomain;
    private final String apiPw;
    private final Long itemPageId;
    private final Long tvPageId;
    private final Long dailyScheCategoryId;
    private final String scheduleImagePath;

    BlogEnum(Long id, String subDomain, String apiPw, Long itemPageId, Long tvPageId, Long dailyScheCategoryId, String scheduleImagePath) {
        this.id = id;
        this.subDomain = subDomain;
        this.apiPw = apiPw;
        this.itemPageId = itemPageId;
        this.tvPageId = tvPageId;
        this.dailyScheCategoryId = dailyScheCategoryId;
        this.scheduleImagePath = scheduleImagePath;
    }

    public static BlogEnum get(Long argId) {
        return Arrays.stream(BlogEnum.values()).filter(e -> e.id.equals(argId)).findFirst().orElse(null);
    }

    public static BlogEnum findBySubdomain(String subDomain) {
        for (BlogEnum e : BlogEnum.values()) {
            if (e.getSubDomain() != null && e.getSubDomain().equals(subDomain)) {
                return e;
            }
        }
        return BlogEnum.MAIN;
    }

    public static List<String> getAllSubdomain() {
        return Arrays.stream(BlogEnum.values()).map(BlogEnum::getSubDomain).distinct().collect(Collectors.toList());
    }
}
