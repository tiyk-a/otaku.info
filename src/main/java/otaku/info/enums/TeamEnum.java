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

    SNOWMAN(6,null,"","","Snow man","snowmanhayainfo", "snowman.", "snowman:k9UR wNsr LzdK IYFT oQ4s gKuJ",18,20),
    KANJANI8(7,null,"カンジャニエイト","関ジャニ","関ジャニ∞","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    SEXYZONE(8,null,"セクシーゾーン","セクゾ","Sexy Zone","sexyz0neinfo", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    TOKIO(9,null,"トキオ","","TOKIO","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    V6(10,null,"ブイシックス","","V6","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    ARASHI(11,null,"アラシ","嵐","ARASHI","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    NEWS(12,null,"ニュース","","NEWS Johnny's","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    KISMYFT2(13,null,"キスマイフットツー","キスマイ","Kis-My-Ft2","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    ABCZ(14,null,"エービーシーズィー","エビ","A.B.C-Z","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    JOHNNYSWEST(15,null,"ジャニーズウェスト","ジャニスト ","ジャニーズWEST ","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    KINGPRINCE(16,null,"キングアンドプリンス ","キンプリ ","King & Prince ","princehayainfo", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    SIXTONES(17,null,"ストーンズ ","ストンズ ","SixTONES ","sixtoneshayain1", "sixtones.", "sixtones:r9Ux DkUr 4cqp or4q FX3c U1sM",20,22),
    NANIWADANSHI(18,null,"ナニワダンシ ","なにわ ","なにわ男子 ","naniwa_hayainfo", "naniwadanshi.", "naniwa:0iqK j9dg a2Ec aQ0h gJOI v0rs",18,20),
    HEYSAYJUMP(19,null,"ヘイセイジャンプ ","JUMP ","Hey! Say! JUMP ","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    KATTUN(20,null,"カトゥーン ","KAT-TUN ","KAT-TUN ","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707),
    KINKIKIDS(21,null,"キンキキッズ ","キンキ ","KinKi Kids ","", "", "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV",33,1707);

    private Integer id;
    private String anniversary;
    private String kana;
    private String mnemonic;
    private String name;
    private String tw_id;
    /** 固有WPブログが用意されてる場合、サブドメインの固有値部分を指定。ない場合、null */
    private String subDomain;
    /** TODO: apiPwに変更したいです */
    private String blogPw;
    private Integer itemPageId;
    private Integer tvPageId;

    TeamEnum(Integer id, String anniversary, String kana, String mnemonic, String name, String tw_id, String subDomain, String blogPw, Integer itemPageId, Integer tvPageId) {
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
        return new Team((long) this.id, this.name, this.kana, this.mnemonic, this.anniversary, this.tw_id, null, null);
    }

    public static TeamEnum get(Integer argId) {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.id.equals(argId)).findFirst().orElse(null);
    }

    public static TeamEnum get(String argName) {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.name.equals(argName)).findFirst().orElse(null);
    }

    public static TeamEnum getBySubDomain(String argSubDomain) {
        if (argSubDomain == null) {
            return null;
        }
        TeamEnum result = null;
        for (TeamEnum e : TeamEnum.values()) {
            System.out.println("TeamEnum: " + e.getSubDomain());
            if (e.getSubDomain() != null && e.getSubDomain().equals(argSubDomain)) {
                result = e;
                break;
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
    public static Integer findIdBySubDomain(String argSubDomain) {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.getSubDomain().equals(argSubDomain)).findFirst().map(TeamEnum::getId).orElse(0);
    }

    /**
     * 引数のTeamIdからサブドメインを返却します。
     * 見つからなかった場合はnullを返します→総合ブログで扱ってね
     *
     * @param argId
     * @return
     */
    public static String findSubDomainById(Integer argId) {
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
        return Arrays.stream(TeamEnum.values()).filter(e -> teamIdList.stream().anyMatch(f -> e.getId().equals(f))).map(TeamEnum::getName).collect(Collectors.toList());
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
        return Arrays.stream(TeamEnum.values()).filter(e -> teamIdList.stream().anyMatch(f ->(e.id.equals(f)))).map(TeamEnum::getName).collect(Collectors.toList());
    }

    public static String getItemPageId(Integer teamId) {
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

    public static String getTvPageId(Integer teamId) {
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
