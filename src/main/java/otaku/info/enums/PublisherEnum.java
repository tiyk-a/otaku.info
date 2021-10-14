package otaku.info.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 出版社Enum
 * 第3引数により、出版社の親子関係が存在します（角川とかが使用）
 * 第3引数の使い方：null->特記なし、0->悪い出版社、n->親出版社
 *
 */
@Getter
public enum PublisherEnum {

    KADOKAWA(1, "KADOKAWA", null),
    SYUEISHA(2, "集英社", null),
    SUNDAY_MAINICHI(3, "サンデー毎日", null),
    ROKUSAISHA(4, "鹿砦社", 0),
    SANKEI_SHUPAN(5, "産経新聞出版", null),
    DIAMOND(6, "ダイヤモンド社", null),
    NEKO_PUB(7, "ネコ・パブリッシング", null),
    BROWNS_BOOKS(8, "ブラウンズブックス", null),
    MAGAZINE_HOUSE(9, "マガジンハウス", null),
    MEDIA_BOY(10, "メディアボーイ", null),
    YOSHIMOTO_BOOKS(11, "ヨシモトブックス", null),
    LEED(12, "リイド社", null),
    WANI_BOOKS(13, "ワニブックス", null),
    ONE_PUB(14, "ワン・パブリッシング", null),
    KADOKAWA_BUNKO(15, "角川文庫", 1),
    GENTOSHA(16, "幻冬舎", null),
    KOBUNSHA(17, "光文社", null),
    KODANSHA(18, "講談社", null),
    SANEI(19, "三栄", null),
    FUTABA(21, "双葉社", null),
    ASAHI_SHUPAN(22, "朝日新聞出版", null),
    TOKYO_NEWS_TUSHIN(23, "東京ニュース通信社", null),
    NIKKEI_BP(24, "日経BPマーケティング", null),
    HINODE_PUB(25, "日之出出版", null),
    TAKARASHIMA(26, "宝島社", null),
    KOSAIDO_PUB(27, "廣済堂出版", null),
    SHOGAKUKAN(28, "小学館", null),
    AZABUDAI(29, "麻布台出版社", null),
    PICTUP(30, "ピクトアップ", null),
    CCC_MEDIA_HOUSE(31, "CCCメディアハウス", null),
    YUDUTSUSYA(32, "夕星社", null),
    ONGAKUTO_HITO(33, "音楽と人", null),
    KADOKAWA_ASCII(34, "角川アスキー総合研究所", 2),
    BUNKA_PUB(35, "文化出版局", null),
    ASAHI_SHINBUN(36, "朝日新聞社", null),
    ASAHI_GAKUSEI_SHINBUN(37, "朝日学生新聞社", 36),
    SHUFUNO_TOMO(38, "主婦の友社", null),
    BUNGEI_SHUNSYU(39, "文藝春秋", null),
    SANKEIDO_PUB(40, "三慧堂出版", null);

    private final Integer id;
    private final String name;

    /** nullは特記なし、0は取り込まない出版社、それ以外の数字は親出版社がある場合（その出版社は子出版社である） */
    private final Integer note;


    PublisherEnum(int id, String name, Integer note) {
        this.id = id;
        this.name = name;
        this.note = note;
    }

    /**
     * IDからenumを取得します。
     *
     * @param id
     * @return
     */
    public static PublisherEnum get(Integer id) {
        return Arrays.stream(PublisherEnum.values()).filter(e -> e.id == (long) id).findFirst().orElse(null);
    }

    /**
     * Stringから出版社Enumを返します
     *
     * @param target
     * @return
     */
    public static PublisherEnum findIdByStringList(String target) {
        PublisherEnum res = null;
        for (String s : target.split(" ")) {
            res = Arrays.stream(PublisherEnum.values()).filter(e -> s.equals(e.name)).findFirst().orElse(null);
            if (res != null) {
                break;
            }
        }
        return res;
    }

    /**
     * 引数の文字列から見つけた雑誌名をIDとともに返却します。
     *
     * @param target
     * @return
     */
    public static List<Long> findPublisherIdByText(String target) {
        return  Arrays.stream(PublisherEnum.values()).filter(e -> target.contains(e.name)).map(e -> (long) e.id).collect(Collectors.toList());
    }

    /**
     * 引数のIDリストから雑誌名をリストにして返却します。
     *
     * @param idList
     * @return
     */
    public static List<String> findPublisherNameList(List<Long> idList) {
        List<String> result = new ArrayList<>();
        for (Long id : idList) {
            PublisherEnum e = PublisherEnum.get(Math.toIntExact(id));
            if (e != null) {
                result.add(e.name);
            }
        }
        return result;
    }
}
