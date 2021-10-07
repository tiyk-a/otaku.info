package otaku.info.enums;

import lombok.Getter;
import otaku.info.entity.Member;

import java.util.Arrays;
import java.util.Date;

@Getter
public enum MemberEnum {

    HIKARU_IWAMOTO(31,new Date(1993-1900,5-1,17),"いわもとひかる","岩本照","ひーくんひかる",6),
    SHINYA_FUKASAWA(32,new Date(1992-1900,5-1,5),"ふかさわしんや","深澤辰哉","ふっか",6),
    SHOTA_WATANABE(33,new Date(1992-1900,11-1,5),"わたなべしょうた","渡辺翔太","しょっぴー",6),
    RYOTA_ABE(34,new Date(1993-1900,11-1,27),"あべりょうた","阿部亮平","あべちゃん",6),
    RYOTA_MIYADATE(35,new Date(1993-1900,3-1,25),"みやだてりょうた","宮舘涼太","舘様",6),
    DAISUKE_SAKUMA(36,new Date(1992-1900,7-1,5),"さくまだいすけ","佐久間大介","さっくん",6),
    KOJI_MUKAI(37,new Date(1994-1900,6-1,21),"むかいこうじ","向井康二","康二じーこ",6),
    REN_MEGURO(38,new Date(1997-1900,2-1,16),"めぐろれん","目黒蓮","めめ",6),
    RAUL(39,new Date(2003-1900,6-1,27),"らうーる","ラウール","ラウール",6),
    YU_YOKOYAMA(40,new Date(1981-1900,5-1,10),"よこやまゆう","横山裕","よこよこちょゆうちん",7),
    SHINGO_MURAKAMI(41,new Date(1982-1900,1-1,26),"むらかみしんご","村上信五","ヒナしんちゃん",7),
    RYUHEI_MARUYAMA(42,new Date(1983-1900,11-1,26),"まるやまりゅうへい","丸山隆平","まるまるちゃん",7),
    SHOTA_YASUDA(43,new Date(1984-1900,9-1,11),"やすだしょうた","安田章大","やすしょうちゃん",7),
    TADAYOSHI_OHKURA(44,new Date(1985-1900,5-1,16),"おおくらただよし","大倉忠義","大倉たっちょん",7),
    KENTO_NAKAJIMA(45,new Date(1994-1900,3-1,13),"なかじまけんと","中島健人","健人ナカケンナカジーラブホリ先輩健人君",8),
    FUMA_KIKUCHI(46,new Date(1995-1900,3-1,7),"きくちふうま","菊池風磨","風磨ふーまきくふうふまたん風磨君",8),
    SHORI_SATO(47,new Date(1996-1900,10-1,30),"さとうしょうり","佐藤勝利","勝利勝利君しょりたん",8),
    SO_MATSUSHIMA(48,new Date(1997-1900,11-1,27),"まつしまそう","松島聡","聡まっつーそうちゃん",8),
    YO_MARIUS(49,new Date(2000-1900,3-1,30),"まりうすよう","マリウス葉","マリウスマリウス君まりちゃんまりたん葉くんマリーまり天使",8),
    JESHY(50,new Date(1996-1900,6-1,11),"ジェシー","ジェシー","ジェシージェス",17),
    TAIGA_KYOMOTO(51,new Date(1994-1900,12-1,3),"きょうもとたいが","京本大我","きょもたいが",17),
    HOKUTO_MATSUMURA(52,new Date(1995-1900,6-1,18),"まつむらほくと","松村北斗","ほくとほっくん",17),
    JURI_TANAKA(53,new Date(1995-1900,6-1,15),"たなかじゅり","田中樹","じゅりじゅったん",17),
    YUGO_KOCHI(54,new Date(1994-1900,3-1,8),"こうちゆうご","高地優吾","こうちおじいちゃん",17),
    SHINTARO_MORIMOTO(55,new Date(1997-1900,7-1,15),"もりもとしんたろう","森本慎太郎","しんたろう慎ちゃん",17),
    SYO_HIRANO(56,new Date(1997-1900,1-1,29),"ひらのしょう","平野紫耀","しょうひらのん",16),
    REN_NAGASE(57,new Date(1999-1900,1-1,23),"ながせれん","永瀬廉","れんれんれんくん",16),
    KAITO_TAKAHASHI(58,new Date(1999-1900,4-1,3),"たかはしかいと","髙橋海人","かいとたかいとかいちゃんかい",16),
    YUTA_KISHI(59,new Date(1995-1900,9-1,29),"きしゆうた","岸優太","岸くんこしくん優太優太くん",16),
    YUTA_JINGUJI(60,new Date(1997-1900,10-1,30),"じんぐうじゆうた","神宮寺勇太","じんじんくんじぐたんチャラぐうじ",16),
    SHIGERU_JOSHIMA(61,new Date(1970-1900,11-1,17),"じょうしましげる","城島茂","リーダー",9),
    TAICHI_KOKUBUN(62,new Date(1974-1900,9-1,2),"こくぶんたいち","国分太一","太一太一くん",9),
    MASAHIRO_MATSUOKA(63,new Date(1977-1900,1-1,11),"まつおかまさひろ","松岡昌宏","松岡マボ",9),
    MASAYUKI_SAKAMOTO(64,new Date(1971-1900,7-1,24),"さかもとまさゆき","坂本昌行","まぁくんまささん坂本くんリーダー",10),
    HIROSHI_NAGANO(65,new Date(1972-1900,10-1,9),"ながのひろし","長野博","長野くん博仏ロイヤル博なーのくん",10),
    YOSHIHIKO_INOHARA(66,new Date(1976-1900,5-1,17),"いのはらよしひこ","井ノ原快彦","いのっち井ノ原くんよしくんよっちゃん",10),
    GO_MORITA(67,new Date(1979-1900,2-1,20),"もりたごう","森田剛","剛くんごぉつん",10),
    KEN_MIYAKE(68,new Date(1979-1900,7-1,2),"みやけけん","三宅健","健くん健ちゃん",10),
    JUNICHI_OKADA(69,new Date(1980-1900,11-1,18),"おかだじゅんいち","岡田准一","岡田くん准くん師範",10),
    SATOSHI_OHNO(70,new Date(1980-1900,11-1,26),"おおのさとし","大野智","大ちゃんおじさん",11),
    SHO_SAKURAI(71,new Date(1982-1900,1-1,25),"さくらいしょう","櫻井翔","翔ちゃん",11),
    MASAKI_AIBA(72,new Date(1982-1900,12-1,24),"あいばまさき","相葉雅紀","相葉ちゃん相葉くん",11),
    KAZUNARI_NINOMIYA(73,new Date(1983-1900,6-1,17),"にのみやかずなり","二宮和也","ニノ",11),
    JUN_MATSUMOTO(74,new Date(1983-1900,8-1,30),"まつもとじゅん","松本潤","松潤",11),
    KEIICHIRO_KOYAMA(75,new Date(1984-1900,5-1,1),"こやまけいいちろう","小山慶一郎","小山けーちゃん",12),
    TAKAHISA_MASUDA(76,new Date(1986-1900,7-1,4),"ますだたかひさ","増田貴久","まっすー",12),
    SHIGEAKI_KATO(77,new Date(1987-1900,7-1,11),"かとうしげあき","加藤シゲアキ","シゲシゲアキ先生",12),
    HIROMITSU_KITAYAMA(78,new Date(1985-1900,9-1,17),"きたやまひろみつ","北山宏光","みっくんみっちゃんミツキタミツ北山宏光",13),
    WATARU_YOKO(79,new Date(1986-1900,5-1,16),"よこおわたる","横尾渉","横尾さんワッターわた渉ワタさん",13),
    TAISUKE_FUJIGAYA(80,new Date(1987-1900,6-1,25),"ふじがやたいすけ","藤ヶ谷太輔","ガヤガヤさん太P太ちゃんガヤ様太輔藤ヶ谷",13),
    TOSHIYA_MIYATA(81,new Date(1988-1900,9-1,14),"みやたとしや","宮田俊哉","宮っち宮田みやとしくん",13),
    YUTA_TAMAMORI(82,new Date(1990-1900,3-1,17),"たまもりゆうた","玉森裕太","玉ちゃん玉裕太玉森ゆうちゃんゆうくん",13),
    TAKASHI_NIKAIDO(83,new Date(1990-1900,8-1,6),"にかいどうたかし","二階堂高嗣","ニカニカちゃん二階堂",13),
    KENTO_SENGA(84,new Date(1991-1900,3-1,23),"せんがけんと","千賀健永","千ちゃんがっちゃん健永千賀けんぴガガ様",13),
    KOICHI_GOSEKI(85,new Date(1985-1900,6-1,17),"ごせきこういち","五関晃一","ごっち五関様",14),
    SYOTA_TOTSUKA(86,new Date(1986-1900,11-1,13),"とつかしょうた","戸塚祥太","とっつ－",14),
    RYOICHI_TSUKADA(87,new Date(1986-1900,12-1,10),"つかだりょういち","塚田僚一","つかちゃん",14),
    FUMITO_KAWAI(88,new Date(1987-1900,10-1,20),"かわいふみと","河合郁人","河合ちゃんふみきゅん",14),
    RYOSUKE_HASHIMOTO(89,new Date(1993-1900,7-1,15),"はしもとりょうすけ","橋本良亮","はっし－",14),
    JUNTA_NAKAMA(90,new Date(1987-1900,10-1,21),"なかまじゅんた","中間淳太","淳太お淳太様じゅんじゅんじゅんてぃ",15),
    TAKAHIRO_HAMADA(91,new Date(1988-1900,12-1,9),"はまだたかひろ","濱田崇裕","濵ちゃんはまだ崇裕なで肩馬",15),
    AKITO_KIRIYAMA(92,new Date(1989-1900,8-1,31),"きりやまあきと","桐山照史","照史てるし親方あっくん榮三郎",15),
    DAIKI_SHIGEOKA(93,new Date(1992-1900,8-1,26),"しげおかだいき","重岡大毅","しげだいきゅん",15),
    TOMOHIRO_KAMIYAMA(94,new Date(1993-1900,7-1,1),"かみやまともひろ","神山智洋","神ちゃんモンチ智くん",15),
    RYUSEI_FUJII(95,new Date(1993-1900,8-1,18),"ふじいりゅうせい","藤井流星","流星流ちゃん",15),
    NOZOMU_KOTAKI(96,new Date(1996-1900,7-1,30),"こたきのぞむ","小瀧望","のんちゃん望のんすけこたっきーノンコタニシ",15),
    DAIGO_NISHIHATA(97,new Date(1997-1900,1-1,9),"にしはただいご","西畑大吾","大ちゃんだいごだいちゅん",18),
    RYUSEI_OHNISHI(98,new Date(2001-1900,8-1,7),"おおにしりゅうせい","大西流星","りゅちぇ流星",18),
    SHUNSUKE_MICHIEDA(99,new Date(2002-1900,7-1,25),"みちえだしゅんすけ","道枝駿佑","しゅんみっちー",18),
    KYOHEI_TAKAHASHI(100,new Date(2000-1900,2-1,28),"たかはしきょうへい","高橋恭平","恭平キョロ",18),
    KENTO_NAGAO(101,new Date(2002-1900,8-1,15),"ながおけんと","長尾謙杜","長尾けんとけんけんけんちゃん",18),
    JOICHIRO_FUJIWARA(102,new Date(1996-1900,2-1,8),"ふじわらじょういちろう","藤原丈一郎","丈丈くん",18),
    KAZUYA_OHASHI(103,new Date(1997-1900,8-1,9),"おおはしかずや","大橋和也","大橋君はっすんかずくん",18);

    private Integer id;
    private Date birthday;
    private String kana;
    private String name;
    private String mnemonic;
    private Integer teamId;

    MemberEnum(Integer id, Date birthday, String kana, String name, String mnemonic, Integer teamId) {
        this.id = id;
        this.birthday = birthday;
        this.kana = kana;
        this.name = name;
        this.mnemonic = mnemonic;
        this.teamId = teamId;
    }

    public Member convertToEntity() {
        return new Member((long) this.id, (long) this.teamId, this.name, this.kana, this.mnemonic, this.birthday, null, null);
    }

    public static MemberEnum get(Integer argId) {
        return Arrays.stream(MemberEnum.values()).filter(e -> e.id.equals(argId)).findFirst().orElse(null);
    }

    public static MemberEnum get(String argName) {
        return Arrays.stream(MemberEnum.values()).filter(e -> e.name.equals(argName)).findFirst().orElse(null);
    }

    public static Long getTeamIdById(Long argMemberId) {
        return Arrays.stream(MemberEnum.values()).filter(e -> e.id.equals(argMemberId)).map(e -> (long) e.getTeamId()).findFirst().orElse(0L);
    }
}
