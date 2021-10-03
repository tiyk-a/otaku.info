package otaku.info.enums;

import lombok.Getter;
import otaku.info.entity.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum TeamEnum {

    SNOWMAN(6,null,"","","Snow man","snowmanhayainfo", "snowman.", null),
    KANJANI8(7,null,"カンジャニエイト","関ジャニ","関ジャニ∞","", null, null),
    SEXYZONE(8,null,"セクシーゾーン","セクゾ","Sexy Zone","sexyz0neinfo", null, null),
    TOKIO(9,null,"トキオ","","TOKIO","", null, null),
    V6(10,null,"ブイシックス","","V6","", null, null),
    ARASHI(11,null,"アラシ","嵐","ARASHI","", null, null),
    NEWS(12,null,"ニュース","","NEWS Johnny's","", null, null),
    KISMYFT2(13,null,"キスマイフットツー","キスマイ","Kis-My-Ft2","", null, null),
    ABCZ(14,null,"エービーシーズィー","エビ","A.B.C-Z","", null, null),
    JOHNNYSWEST(15,null,"ジャニーズウェスト","ジャニスト ","ジャニーズWEST ","", null, null),
    KINGPRINCE(16,null,"キングアンドプリンス ","キンプリ ","King & Prince ","princehayainfo", "kingandprince.", null),
    SIXTONES(17,null,"ストーンズ ","ストンズ ","SixTONES ","sixtoneshayain1", "sixtones.", null),
    NANIWADANSHI(18,null,"ナニワダンシ ","なにわ ","なにわ男子 ","naniwa_hayainfo", "naniwadanshi.", null),
    HEYSAYJUMP(19,null,"ヘイセイジャンプ ","JUMP ","Hey! Say! JUMP ","", null, null),
    KATTUN(20,null,"カトゥーン ","KAT-TUN ","KAT-TUN ","", null, null),
    KINKIKIDS(21,null,"キンキキッズ ","キンキ ","KinKi Kids ","", null, null);

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

    TeamEnum(Integer id, String anniversary, String kana, String mnemonic, String name, String tw_id, String subDomain, String blogPw) {
        this.id = id;
        this.anniversary = anniversary;
        this.kana = kana;
        this.mnemonic = mnemonic;
        this.name = name;
        this.tw_id = tw_id;
        this.subDomain = subDomain;
        this.blogPw = blogPw;
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
        return Arrays.stream(TeamEnum.values()).filter(e -> e.getSubDomain().equals(argSubDomain)).findFirst().orElse(null);
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
        return Arrays.stream(TeamEnum.values()).filter(e -> e.getSubDomain() != null).map(e -> e.getSubDomain()).collect(Collectors.toList());
    }
}
