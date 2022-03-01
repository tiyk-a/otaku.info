package otaku.info.enums;

import lombok.Getter;
import otaku.info.entity.Member;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum MemberEnum {

    /** 誕生日はyyyy-1900, m-1, dd */
    HIKARU_IWAMOTO(31L,new Date(1993-1900,5-1,17),"いわもとひかる","岩本照","ひーくんひかる",6L),
    SHINYA_FUKASAWA(32L,new Date(1992-1900,5-1,5),"ふかさわしんや","深澤辰哉","ふっか",6L),
    SHOTA_WATANABE(33L,new Date(1992-1900,11-1,5),"わたなべしょうた","渡辺翔太","しょっぴー",6L),
    RYOTA_ABE(34L,new Date(1993-1900,11-1,27),"あべりょうた","阿部亮平","あべちゃん",6L),
    RYOTA_MIYADATE(35L,new Date(1993-1900,3-1,25),"みやだてりょうた","宮舘涼太","舘様",6L),
    DAISUKE_SAKUMA(36L,new Date(1992-1900,7-1,5),"さくまだいすけ","佐久間大介","さっくん",6L),
    KOJI_MUKAI(37L,new Date(1994-1900,6-1,21),"むかいこうじ","向井康二","康二じーこ",6L),
    REN_MEGURO(38L,new Date(1997-1900,2-1,16),"めぐろれん","目黒蓮","めめ",6L),
    RAUL(39L,new Date(2003-1900,6-1,27),"らうーる","ラウール","ラウール",6L),
    YU_YOKOYAMA(40L,new Date(1981-1900,5-1,10),"よこやまゆう","横山裕","よこよこちょゆうちん",7L),
    SHINGO_MURAKAMI(41L,new Date(1982-1900,1-1,26),"むらかみしんご","村上信五","ヒナしんちゃん",7L),
    RYUHEI_MARUYAMA(42L,new Date(1983-1900,11-1,26),"まるやまりゅうへい","丸山隆平","まるまるちゃん",7L),
    SHOTA_YASUDA(43L,new Date(1984-1900,9-1,11),"やすだしょうた","安田章大","やすしょうちゃん",7L),
    TADAYOSHI_OHKURA(44L,new Date(1985-1900,5-1,16),"おおくらただよし","大倉忠義","大倉たっちょん",7L),
    KENTO_NAKAJIMA(45L,new Date(1994-1900,3-1,13),"なかじまけんと","中島健人","健人ナカケンナカジーラブホリ先輩健人君",8L),
    FUMA_KIKUCHI(46L,new Date(1995-1900,3-1,7),"きくちふうま","菊池風磨","風磨ふーまきくふうふまたん風磨君",8L),
    SHORI_SATO(47L,new Date(1996-1900,10-1,30),"さとうしょうり","佐藤勝利","勝利勝利君しょりたん",8L),
    SO_MATSUSHIMA(48L,new Date(1997-1900,11-1,27),"まつしまそう","松島聡","聡まっつーそうちゃん",8L),
    YO_MARIUS(49L,new Date(2000-1900,3-1,30),"まりうすよう","マリウス葉","マリウスマリウス君まりちゃんまりたん葉くんマリーまり天使",8L),
    JESHY(50L,new Date(1996-1900,6-1,11),"ジェシー","ジェシー","ジェシージェス",17L),
    TAIGA_KYOMOTO(51L,new Date(1994-1900,12-1,3),"きょうもとたいが","京本大我","きょもたいが",17L),
    HOKUTO_MATSUMURA(52L,new Date(1995-1900,6-1,18),"まつむらほくと","松村北斗","ほくとほっくん",17L),
    JURI_TANAKA(53L,new Date(1995-1900,6-1,15),"たなかじゅり","田中樹","じゅりじゅったん",17L),
    YUGO_KOCHI(54L,new Date(1994-1900,3-1,8),"こうちゆうご","高地優吾","こうちおじいちゃん",17L),
    SHINTARO_MORIMOTO(55L,new Date(1997-1900,7-1,15),"もりもとしんたろう","森本慎太郎","しんたろう慎ちゃん",17L),
    SYO_HIRANO(56L,new Date(1997-1900,1-1,29),"ひらのしょう","平野紫耀","しょうひらのん",16L),
    REN_NAGASE(57L,new Date(1999-1900,1-1,23),"ながせれん","永瀬廉","れんれんれんくん",16L),
    KAITO_TAKAHASHI(58L,new Date(1999-1900,4-1,3),"たかはしかいと","髙橋海人","かいとたかいとかいちゃんかい",16L),
    YUTA_KISHI(59L,new Date(1995-1900,9-1,29),"きしゆうた","岸優太","岸くんこしくん優太優太くん",16L),
    YUTA_JINGUJI(60L,new Date(1997-1900,10-1,30),"じんぐうじゆうた","神宮寺勇太","じんじんくんじぐたんチャラぐうじ",16L),
    SHIGERU_JOSHIMA(61L,new Date(1970-1900,11-1,17),"じょうしましげる","城島茂","リーダー",9L),
    TAICHI_KOKUBUN(62L,new Date(1974-1900,9-1,2),"こくぶんたいち","国分太一","太一太一くん",9L),
    MASAHIRO_MATSUOKA(63L,new Date(1977-1900,1-1,11),"まつおかまさひろ","松岡昌宏","松岡マボ",9L),
    MASAYUKI_SAKAMOTO(64L,new Date(1971-1900,7-1,24),"さかもとまさゆき","坂本昌行","まぁくんまささん坂本くんリーダー",10L),
    HIROSHI_NAGANO(65L,new Date(1972-1900,10-1,9),"ながのひろし","長野博","長野くん博仏ロイヤル博なーのくん",10L),
    YOSHIHIKO_INOHARA(66L,new Date(1976-1900,5-1,17),"いのはらよしひこ","井ノ原快彦","いのっち井ノ原くんよしくんよっちゃん",10L),
    GO_MORITA(67L,new Date(1979-1900,2-1,20),"もりたごう","森田剛","剛くんごぉつん",10L),
    KEN_MIYAKE(68L,new Date(1979-1900,7-1,2),"みやけけん","三宅健","健くん健ちゃん",10L),
    JUNICHI_OKADA(69L,new Date(1980-1900,11-1,18),"おかだじゅんいち","岡田准一","岡田くん准くん師範",10L),
    SATOSHI_OHNO(70L,new Date(1980-1900,11-1,26),"おおのさとし","大野智","大ちゃんおじさん",11L),
    SHO_SAKURAI(71L,new Date(1982-1900,1-1,25),"さくらいしょう","櫻井翔","翔ちゃん",11L),
    MASAKI_AIBA(72L,new Date(1982-1900,12-1,24),"あいばまさき","相葉雅紀","相葉ちゃん相葉くん",11L),
    KAZUNARI_NINOMIYA(73L,new Date(1983-1900,6-1,17),"にのみやかずなり","二宮和也","ニノ",11L),
    JUN_MATSUMOTO(74L,new Date(1983-1900,8-1,30),"まつもとじゅん","松本潤","松潤",11L),
    KEIICHIRO_KOYAMA(75L,new Date(1984-1900,5-1,1),"こやまけいいちろう","小山慶一郎","小山けーちゃん",12L),
    TAKAHISA_MASUDA(76L,new Date(1986-1900,7-1,4),"ますだたかひさ","増田貴久","まっすー",12L),
    SHIGEAKI_KATO(77L,new Date(1987-1900,7-1,11),"かとうしげあき","加藤シゲアキ","シゲシゲアキ先生",12L),
    HIROMITSU_KITAYAMA(78L,new Date(1985-1900,9-1,17),"きたやまひろみつ","北山宏光","みっくんみっちゃんミツキタミツ北山宏光",13L),
    WATARU_YOKO(79L,new Date(1986-1900,5-1,16),"よこおわたる","横尾渉","横尾さんワッターわた渉ワタさん",13L),
    TAISUKE_FUJIGAYA(80L,new Date(1987-1900,6-1,25),"ふじがやたいすけ","藤ヶ谷太輔","ガヤガヤさん太P太ちゃんガヤ様太輔藤ヶ谷",13L),
    TOSHIYA_MIYATA(81L,new Date(1988-1900,9-1,14),"みやたとしや","宮田俊哉","宮っち宮田みやとしくん",13L),
    YUTA_TAMAMORI(82L,new Date(1990-1900,3-1,17),"たまもりゆうた","玉森裕太","玉ちゃん玉裕太玉森ゆうちゃんゆうくん",13L),
    TAKASHI_NIKAIDO(83L,new Date(1990-1900,8-1,6),"にかいどうたかし","二階堂高嗣","ニカニカちゃん二階堂",13L),
    KENTO_SENGA(84L,new Date(1991-1900,3-1,23),"せんがけんと","千賀健永","千ちゃんがっちゃん健永千賀けんぴガガ様",13L),
    KOICHI_GOSEKI(85L,new Date(1985-1900,6-1,17),"ごせきこういち","五関晃一","ごっち五関様",14L),
    SYOTA_TOTSUKA(86L,new Date(1986-1900,11-1,13),"とつかしょうた","戸塚祥太","とっつ－",14L),
    RYOICHI_TSUKADA(87L,new Date(1986-1900,12-1,10),"つかだりょういち","塚田僚一","つかちゃん",14L),
    FUMITO_KAWAI(88L,new Date(1987-1900,10-1,20),"かわいふみと","河合郁人","河合ちゃんふみきゅん",14L),
    RYOSUKE_HASHIMOTO(89L,new Date(1993-1900,7-1,15),"はしもとりょうすけ","橋本良亮","はっし－",14L),
    JUNTA_NAKAMA(90L,new Date(1987-1900,10-1,21),"なかまじゅんた","中間淳太","淳太お淳太様じゅんじゅんじゅんてぃ",15L),
    TAKAHIRO_HAMADA(91L,new Date(1988-1900,12-1,9),"はまだたかひろ","濱田崇裕","濵ちゃんはまだ崇裕なで肩馬",15L),
    AKITO_KIRIYAMA(92L,new Date(1989-1900,8-1,31),"きりやまあきと","桐山照史","照史てるし親方あっくん榮三郎",15L),
    DAIKI_SHIGEOKA(93L,new Date(1992-1900,8-1,26),"しげおかだいき","重岡大毅","しげだいきゅん",15L),
    TOMOHIRO_KAMIYAMA(94L,new Date(1993-1900,7-1,1),"かみやまともひろ","神山智洋","神ちゃんモンチ智くん",15L),
    RYUSEI_FUJII(95L,new Date(1993-1900,8-1,18),"ふじいりゅうせい","藤井流星","流星流ちゃん",15L),
    NOZOMU_KOTAKI(96L,new Date(1996-1900,7-1,30),"こたきのぞむ","小瀧望","のんちゃん望のんすけこたっきーノンコタニシ",15L),
    DAIGO_NISHIHATA(97L,new Date(1997-1900,1-1,9),"にしはただいご","西畑大吾","大ちゃんだいごだいちゅん",18L),
    RYUSEI_OHNISHI(98L,new Date(2001-1900,8-1,7),"おおにしりゅうせい","大西流星","りゅちぇ流星",18L),
    SHUNSUKE_MICHIEDA(99L,new Date(2002-1900,7-1,25),"みちえだしゅんすけ","道枝駿佑","しゅんみっちー",18L),
    KYOHEI_TAKAHASHI(100L,new Date(2000-1900,2-1,28),"たかはしきょうへい","高橋恭平","恭平キョロ",18L),
    KENTO_NAGAO(101L,new Date(2002-1900,8-1,15),"ながおけんと","長尾謙杜","長尾けんとけんけんけんちゃん",18L),
    JOICHIRO_FUJIWARA(102L,new Date(1996-1900,2-1,8),"ふじわらじょういちろう","藤原丈一郎","丈丈くん",18L),
    KAZUYA_OHASHI(103L,new Date(1997-1900,8-1,9),"おおはしかずや","大橋和也","大橋君はっすんかずくん",18L),
    RYOSUKE_YAMADA(104L,new Date(1993-1900,5-1,9), "やまだりょうすけ","山田涼介","山ちゃん山田くん涼介くん",19L),
    YURI_CHINEN(105L,new Date(1993-1900,11-1,30), "ちねんゆうり","知念侑李","ちぃちゃん知念ちゃん 侑李くん",19L),
    YUTO_NAKAJIMA(106L,new Date(1993-1900,8-1,10), "なかじまゆうと","中島裕翔","ゆーてぃー",19L),
    DAIKI_ARIOKA(107L,new Date(1991-1900,4-1,15), "ありおかだいき","有岡大貴","大ちゃん",19L),
    YUYA_TAKAKI(108L,new Date(1990-1900,2-1,26), "たかきゆうや","高木雄也","髙木くん雄也くん",19L),
    KEI_INOO(109L,new Date(1990-1900,6-1,22), "いのおけい","伊野尾慧","伊野尾ちゃん",19L),
    HIKARU_YAOTOME(110L,new Date(1990-1900,12-1,2), "やおとめひかる","八乙女光","光くん",19L),
    KOTA_YABU(111L,new Date(1990-1900,1-1,31), "やぶこうた","薮宏太","薮くん",19L),
    KAZUYA_KAMENASHI(112L,new Date(1986-1900,2-1,23), "かめなしかずや","亀梨和也","亀亀ちゃん",20L),
    TATSUYA_UEDA(113L,new Date(1983-1900,10-1,4), "うえだたつや","上田竜也","たっちゃん上ぽむ",20L),
    YUICHI_NAKAMARU(114L,new Date(1983-1900,9-1,4), "なかまるゆういち","中丸雄一","ゆっち鼻鼻丸ポット",20L),
    KOICHI_DOMOTO(114L,new Date(1979-1900,1-1,1), "どうもとこういち","堂本光一","王子",21L),
    TSUYOSHI_DOMOTO(114L,new Date(1979-1900,4-1,10), "どうもとつよし","堂本剛","剛くん",21L);

    private final Long id;
    private final Date birthday;
    private final String kana;
    private final String name;
    private final String mnemonic;
    private final Long teamId;

    MemberEnum(Long id, Date birthday, String kana, String name, String mnemonic, Long teamId) {
        this.id = id;
        this.birthday = birthday;
        this.kana = kana;
        this.name = name;
        this.mnemonic = mnemonic;
        this.teamId = teamId;
    }

    public Member convertToEntity() {
        return new Member( this.id,  this.teamId, this.name, this.kana, this.mnemonic, this.birthday, null, null);
    }

    public static MemberEnum get(Long argId) {
        return Arrays.stream(MemberEnum.values()).filter(e -> e.id.equals(argId)).findFirst().orElse(null);
    }

    public static MemberEnum get(String argName) {
        return Arrays.stream(MemberEnum.values()).filter(e -> e.name.equals(argName)).findFirst().orElse(null);
    }

    public static Long getTeamIdById(Long argMemberId) {
        return Arrays.stream(MemberEnum.values()).filter(e -> e.id.equals(argMemberId)).map(e -> e.getTeamId()).findFirst().orElse(0L);
    }

    public static List<String> findMNameListByIdList(List<Long> mIdList) {
        return Arrays.stream(MemberEnum.values()).filter(e -> mIdList.stream().anyMatch(f ->(e.id.equals(f)))).map(MemberEnum::getName).collect(Collectors.toList());
    }
}
