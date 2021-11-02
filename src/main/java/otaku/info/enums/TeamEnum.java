package otaku.info.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;
import otaku.info.entity.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum TeamEnum {

    SNOWMAN(6L,null,"","SnowMan","Snow man","snowmanhayainfo", "https://snowman.otakuinfo.fun/", "snowman:k9UR wNsr LzdK IYFT oQ4s gKuJ",18L,20L, 39L),
    KANJANI8(7L,null,"カンジャニエイト","関ジャニ","関ジャニ∞","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    SEXYZONE(8L,null,"セクシーゾーン","SexyZone","Sexy Zone","sexyz0neinfo", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    TOKIO(9L,null,"トキオ","TOKIO","TOKIO","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    V6(10L,null,"ブイシックス","V6","V6","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    ARASHI(11L,null,"アラシ","嵐","ARASHI","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    NEWS(12L,null,"ニュース","NEWS","NEWS Johnny's","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    KISMYFT2(13L,null,"キスマイフットツー","KisMyFt2","Kis-My-Ft2","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    ABCZ(14L,null,"エービーシーズィー","ABCZ","A.B.C-Z","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    JOHNNYSWEST(15L,null,"ジャニーズウェスト","ジャニーズWEST","ジャニーズWEST ","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    KINGPRINCE(16L,null,"キングアンドプリンス ","KingandPrince","King & Prince","princehayainfo", "https://kingandprince.otakuinfo.fun/", "king:5Hsj xJot J6Ez jkA1 ZXse ELwX",18L,20L, 35L),
    SIXTONES(17L,null,"ストーンズ ","SixTONES","SixTONES ","sixtoneshayain1", "https://sixtones.otakuinfo.fun/", "sixtones:r9Ux DkUr 4cqp or4q FX3c U1sM",20L,22L, 52L),
    NANIWADANSHI(18L,null,"ナニワダンシ ","なにわ男子","なにわ男子 ","naniwa_hayainfo", "https://naniwadanshi.otakuinfo.fun/", "naniwa:0iqK j9dg a2Ec aQ0h gJOI v0rs",18L,20L, 27L),
    HEYSAYJUMP(19L,null,"ヘイセイジャンプ ","HeySayJUMP","Hey! Say! JUMP ","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    KATTUN(20L,null,"カトゥーン ","KATTUN","KAT-TUN ","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L),
    KINKIKIDS(21L,null,"キンキキッズ ","KinkiKids","KinKi Kids ","", "https://otakuinfo.fun/", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",43L,1707L, 176L);
    private final Long id;
    private final String anniversary;
    private final String kana;
    // tagNameに利用
    private final String mnemonic;
    private final String name;
    private final String tw_id;
    /** 固有WPブログが用意されてる場合、サブドメインの固有値部分を指定。ない場合、null */
    private final String subDomain;
    /** TODO: apiPwに変更したいです */
    private final String blogPw;
    private final Long itemPageId;
    private final Long tvPageId;
    private final Long dailyScheCategoryId;

    TeamEnum(Long id, String anniversary, String kana, String mnemonic, String name, String tw_id, String subDomain, String blogPw, Long itemPageId, Long tvPageId, Long dailyScheCategoryId) {
        this.id = id;
        this.anniversary = anniversary;
        this.kana = kana;
        this.mnemonic = mnemonic;
        this.name = name;
        this.tw_id = tw_id;
        this.subDomain = subDomain;
        this.blogPw = blogPw;
        this.itemPageId = itemPageId;
        this.tvPageId = tvPageId;
        this.dailyScheCategoryId = dailyScheCategoryId;
    }

    public Team convertToEntity() {
        return new Team(this.id, this.name, this.kana, this.mnemonic, this.anniversary, this.tw_id, null, null);
    }

    public static TeamEnum get(Long argId) {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.id.equals(argId)).findFirst().orElse(null);
    }

    public static TeamEnum get(String argName) {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.name.equals(argName)).findFirst().orElse(null);
    }

    // TODO: メソッドめいと処理に乖離がある。メソッドの使用箇所において、subDomainではなくnameを渡しているところがありそうなので、nameでも引っ掛かるようにしてみる
    public static TeamEnum getBySubDomain(String argSubDomain) {
        if (argSubDomain == null) {
            // なんとなくデフォはえびさん
            return TeamEnum.ABCZ;
        }
        TeamEnum result = null;
        for (TeamEnum e : TeamEnum.values()) {
            if (e.getSubDomain() != null && e.getSubDomain().equals(argSubDomain)) {
                result = e;
                break;
            }
        }

        if (result == null) {
            for (TeamEnum e : TeamEnum.values()) {
                if (e.getName() != null
                        && e.getName()
                        .equals(argSubDomain)) {
                    result = e;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 引数のサブドメインからTeamIdを返却します。
     * 見つからなかった場合は0を返します→総合ブログで扱ってね
     *
     * @param argSubDomain
     * @return
     */
    public static Long findIdBySubDomain(String argSubDomain) {
        // teamIdがnullの場合、デフォルト（としてえび）のteamIdを入れる
        Long teamId = TeamEnum.ABCZ.getId();
        for (TeamEnum e : TeamEnum.values()) {
            if (e.getSubDomain().equals(argSubDomain)) {
                teamId = e.getId();
                break;
            }
        }
        return teamId;
    }

    /**
     * 引数のTeamIdからサブドメインを返却します。
     * 見つからなかった場合はnullを返します→総合ブログで扱ってね
     *
     * @param argId
     * @return
     */
    public static String findSubDomainById(Long argId) {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.id.equals(argId)).findFirst().map(e -> e.getSubDomain()).orElse(null);
    }

    /**
     * IDリストからサブドメインのリストを返します。
     *
     * @param teamIdList
     * @return
     */
    public static List<String> findSubDomainListByIdList(List<Long> teamIdList) {
        if (teamIdList == null || teamIdList.size() == 0) {
            return new ArrayList<>();
        }
        return Arrays.stream(TeamEnum.values()).filter(e -> teamIdList.stream().anyMatch(f -> e.getId().equals(f))).map(TeamEnum::getSubDomain).collect(Collectors.toList());
    }

    /**
     * 存在するSubdomainをリストにして返します
     *
     * @return
     */
    public static List<String> getAllSubDomain() {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.getSubDomain() != null).map(TeamEnum::getSubDomain).distinct().collect(Collectors.toList());
    }

    /**
     * 引数のTeamIdListからチーム名リストを返却します。
     *
     * @param teamIdList
     * @return
     */
    public static List<String> findTeamNameListByTeamIdList(List<Long> teamIdList) {
        if (teamIdList == null || teamIdList.get(0) == null) {
            return new ArrayList<>();
        }
        List<String> resultList = new ArrayList<>();

        for (Long teamId : teamIdList) {
            for (TeamEnum e : TeamEnum.values()) {
                if (teamId == null || teamId.equals(0L)) {
                    continue;
                }
                if (e.getId().equals(teamId)) {
                    resultList.add(e.getName());
                    break;
                }
            }
        }
        return resultList;
    }

    /**
     * 引数のTeamIdListからチーム名リストを返却します。
     *
     * @param teamIdList
     * @return
     */
    public static List<String> findMnemonicListByTeamIdList(List<Long> teamIdList) {
        if (teamIdList == null || teamIdList.get(0) == null) {
            return new ArrayList<>();
        }
        List<String> resultList = new ArrayList<>();

        for (Long teamId : teamIdList) {
            for (TeamEnum e : TeamEnum.values()) {
                if (teamId == null || teamId.equals(0L)) {
                    continue;
                }
                if (e.getId().equals(teamId)) {
                    resultList.add(e.getMnemonic());
                    break;
                }
            }
        }
        return resultList;
    }

    public static String getItemPageId(Long teamId) {
        if (teamId == null) {
            return "";
        }
        TeamEnum result = null;
        for (TeamEnum e : TeamEnum.values()) {
            if (e.getId() != null && e.getId().equals(teamId)) {
                result = e;
                break;
            }
        }
        assert result != null;
        return result.getItemPageId().toString();
    }

    public static String getTvPageId(Long teamId) {
        if (teamId == null) {
            return "";
        }
        TeamEnum result = null;
        for (TeamEnum e : TeamEnum.values()) {
            if (e.getId() != null && e.getId().equals(teamId)) {
                result = e;
                break;
            }
        }
        assert result != null;
        return result.getTvPageId().toString();
    }

    public static String getTvPageIdBySubDomain(String subDomain) {
        if (StringUtils.hasText(subDomain)) {
            TeamEnum result = null;
            for (TeamEnum e : TeamEnum.values()) {
                if (e.getId() != null && e.getSubDomain().equals(subDomain)) {
                    result = e;
                    break;
                }
            }
            assert result != null;
            return result.getTvPageId().toString();
        } else {
            // subDOmainがnull/空欄の場合は総合ブログに投稿する
            return "33";
        }
    }
}
