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

    SNOWMAN(6L,null,"","","Snow man","snowmanhayainfo", "snowman.", "snowman:k9UR wNsr LzdK IYFT oQ4s gKuJ",18L,20L),
    KANJANI8(7L,null,"カンジャニエイト","関ジャニ","関ジャニ∞","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    SEXYZONE(8L,null,"セクシーゾーン","セクゾ","Sexy Zone","sexyz0neinfo", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    TOKIO(9L,null,"トキオ","","TOKIO","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    V6(10L,null,"ブイシックス","","V6","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    ARASHI(11L,null,"アラシ","嵐","ARASHI","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    NEWS(12L,null,"ニュース","","NEWS Johnny's","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    KISMYFT2(13L,null,"キスマイフットツー","キスマイ","Kis-My-Ft2","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    ABCZ(14L,null,"エービーシーズィー","エビ","A.B.C-Z","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    JOHNNYSWEST(15L,null,"ジャニーズウェスト","ジャニスト ","ジャニーズWEST ","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    KINGPRINCE(16L,null,"キングアンドプリンス ","キンプリ ","King & Prince","princehayainfo", "kingandprince.", "king:5Hsj xJot J6Ez jkA1 ZXse ELwX",18L,20L),
    SIXTONES(17L,null,"ストーンズ ","ストンズ ","SixTONES ","sixtoneshayain1", "sixtones.", "sixtones:r9Ux DkUr 4cqp or4q FX3c U1sM",20L,22L),
    NANIWADANSHI(18L,null,"ナニワダンシ ","なにわ ","なにわ男子 ","naniwa_hayainfo", "naniwadanshi.", "naniwa:0iqK j9dg a2Ec aQ0h gJOI v0rs",18L,20L),
    HEYSAYJUMP(19L,null,"ヘイセイジャンプ ","JUMP ","Hey! Say! JUMP ","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    KATTUN(20L,null,"カトゥーン ","KAT-TUN ","KAT-TUN ","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L),
    KINKIKIDS(21L,null,"キンキキッズ ","キンキ ","KinKi Kids ","", "NA", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33L,1707L);
    private Long id;
    private String anniversary;
    private String kana;
    private String mnemonic;
    private String name;
    private String tw_id;
    /** 固有WPブログが用意されてる場合、サブドメインの固有値部分を指定。ない場合、null */
    private String subDomain;
    /** TODO: apiPwに変更したいです */
    private String blogPw;
    private Long itemPageId;
    private Long tvPageId;

    TeamEnum(Long id, String anniversary, String kana, String mnemonic, String name, String tw_id, String subDomain, String blogPw, Long itemPageId, Long tvPageId) {
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
            return null;
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
        return Arrays.stream(TeamEnum.values()).filter(e -> e.getSubDomain() != null).map(TeamEnum::getSubDomain).collect(Collectors.toList());
    }

    /**
     * 引数のTeamIdListからチーム名リストを返却します。
     *
     * @param teamIdList
     * @return
     */
    public static List<String> findTeamNameListByTeamIdList(List<Long> teamIdList) {
        if (teamIdList == null || teamIdList.get(0) == null) {
            return null;
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

    public static String getItemPageId(Long teamId) {
        if (teamId == null) {
            return null;
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
            return null;
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
