package otaku.info.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum TeamEnum {

    SNOWMAN(6L,null,"","SnowMan","Snow Man","snowmanhayainfo", "gfb6rs9140td7etmbup4qeu95c@group.calendar.google.com", "https://mentrecording.jp/snowman/", null, 6L, null),
    KANJANI8(7L,null,"カンジャニエイト","関ジャニ","関ジャニ∞","", "1sb8fb0nlu2l7t8hc1fsncau2g@group.calendar.google.com", "https://www.infinity-r.jp/", null, 7L, null),
    SEXYZONE(8L,null,"セクシーゾーン","SexyZone","Sexy Zone","sexyz0neinfo", "kmikj1iusd3j8rquta40adqjek@group.calendar.google.com", "https://topjrecords.jp/", null, 7L, null),
    TOKIO(9L,null,"トキオ","TOKIO","TOKIO","", "jjr7ntm72bhm2kpmu7im4p8de0@group.calendar.google.com", "https://www.tokio.inc/s/tokio/", null, 7L, null),
    V6(10L,null,"ブイシックス","V6","V6","", "jivfndb5tl2jhrie3jj0mg3rf4@group.calendar.google.com", "https://avex.jp/v6/", null, 7L, null),
    ARASHI(11L,null,"アラシ","嵐","ARASHI","", "fdb4l5ap4c90alatvikqqp6dfo@group.calendar.google.com", "https://www.j-storm.co.jp/s/js/artist/J0004", null, 7L, true),
    NEWS(12L,null,"ニュース","NEWS","NEWS ジャニーズ","", "vunh6s6f3n5emin2kb3cgv1278@group.calendar.google.com", "https://www.jehp.jp/s/je/artist/J0005?ima=2344", null, 7L, true),
    KISMYFT2(13L,null,"キスマイフットツー","KisMyFt2","Kis-My-Ft2","", "n35frscj6ds1p6nfopkdo60vmk@group.calendar.google.com", "https://mentrecording.jp/kismyft2/", null, 7L, null),
    ABCZ(14L,null,"エービーシーズィー","ABCZ","A.B.C-Z","", "of5nq9o9g4k5pj11bvt1rr5168@group.calendar.google.com", "https://abcz.ponycanyon.co.jp/", null, 7L, null),
    JOHNNYSWEST(15L,null,"ジャニーズウェスト","ジャニーズWEST","ジャニーズWEST ","", "ico4t9mlh9fd4smc47ptf2g3h8@group.calendar.google.com", "https://www.jehp.jp/s/je/artist/J0010", null, 7L, null),
    KINGPRINCE(16L,null,"キングアンドプリンス ","KingandPrince","King & Prince","princehayainfo", "93v42jd3m5tkf2e7k42fa1id34@group.calendar.google.com", "https://www.universal-music.co.jp/king-and-prince/", null, 16L, false),
    SIXTONES(17L,null,"ストーンズ ","SixTONES","SixTONES","sixtoneshayain1", "kan71rrmb42l2mh1qnp5br1hb0@group.calendar.google.com", "https://www.sixtones.jp/", null, 17L, null),
    NANIWADANSHI(18L,null,"ナニワダンシ ","なにわ男子","なにわ男子","naniwa_hayainfo", "enf647q0ka2ijj35n9ibvmdbbg@group.calendar.google.com", "https://www.j-storm.co.jp/s/js/artist/J0011", null, 18L, null),
    HEYSAYJUMP(19L,null,"ヘイセイジャンプ ","HeySayJUMP","Hey! Say! JUMP","", "pjlojsmpi6vjhmu4v3ve6a5jlo@group.calendar.google.com", "https://www.j-storm.co.jp/s/js/artist/J0007", null, 7L, null),
    KATTUN(20L,null,"カトゥーン ","KATTUN","KAT-TUN","", "1mol4ar70n9ch4737rg8s6bs3k@group.calendar.google.com", "https://www.j-storm.co.jp/s/js/artist/J0006", null, 7L, null),
    KINKIKIDS(21L,null,"キンキキッズ ","KinkiKids","KinKi Kids","", "16o2mrgjfscpti4pib9stma0b8@group.calendar.google.com", "https://www.jehp.jp/s/je/artist/J0003", null, 7L, null);

    private final Long id;
    private final String anniversary;
    private final String kana;

    // tagNameに利用する記号やスペースを抜いた名称
    private final String mnemonic;

    // 正式名称
    private final String name;
    private final String tw_id;
    private final String calendarId;
    /** SEO対策のためexternal linkを必ずwordpress投稿に入れたい */
    private final String officialSite;
    /** SEO対策のためinternal linkを必ずwordpress投稿に入れたい。nullの場合、subdomainとかをそのまま使えばいいのでは */
    private final String internalTop;
    /** BlogEnumのID */
    private final Long blogEnumId;

    /** TVデータの詳細をチェックする時に値を入れる。メンバー名だけでチェックするならtrue,チーム名からチェックしていいならfalse,詳細チェックしないならnull */
    private final Boolean chkTvDetailByMemName;

    TeamEnum(Long id, String anniversary, String kana, String mnemonic, String name, String tw_id, String calendarId, String officialSite, String internalTop, Long blogEnumId, Boolean chkTvDetailByMemName) {
        this.id = id;
        this.anniversary = anniversary;
        this.kana = kana;
        this.mnemonic = mnemonic;
        this.name = name;
        this.tw_id = tw_id;
        this.calendarId = calendarId;
        this.officialSite = officialSite;
        this.internalTop = internalTop;
        this.blogEnumId = blogEnumId;
        this.chkTvDetailByMemName = chkTvDetailByMemName;
    }

    public static TeamEnum get(Long argId) {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.id.equals(argId)).findFirst().orElse(null);
    }

    public static TeamEnum get(String argName) {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.name.equals(argName)).findFirst().orElse(getByMnemonic(argName));
    }

    public static TeamEnum getByMnemonic(String argName) {
        return Arrays.stream(TeamEnum.values()).filter(e -> e.mnemonic.equals(argName)).findFirst().orElse(null);
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
                    resultList.add(e.getName());
                    break;
                }
            }
        }
        return resultList;
    }
}
