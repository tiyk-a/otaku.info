package otaku.info.enums;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 既知の雑誌媒体はenum管理をする。
 * 見つからなかった媒体はDBに保管して対応。
 * 運用：①記号が入る雑誌名は半角スペースで区切って格納しています。よって、regexを使って合致条件を作って使用すること
 * ex.) Cinema★Cinemaは"Cinema Cinema"として格納。regexは"Cinema.?Cinema"とすることで、下記の条件に合致できる
 * 「CinemaCinema」[Cinema Cinema][Cinema★Cinema][Cinema・Cinema]...など、CinemaとCinemaの間に何か1文字ありorなしの場合に合致
 *
 * 運用：②Exclamation mark(!)は全角で登録してる場合、半角に直してあげる処理入れないと引っかからないよ
 *
 * TODO: 随時、manually DB→enumに移行することでDBアクセスを減らすこと
 *
 */
public enum MagazineEnum {

    TARZAM(1,"Tarzan","ターザン",9),
    AERA(2,"AERA","アエラ",22),
    AERA_ENG(3,"AERA English","アエラ・イングリッシュ",22),
    ANAN(4,"an an","アンアン",9),
    BAILA(5,"BAILA","バイラ",2),
    BRUTUS(6,"BRUTUS","ブルータス",9),
    CANCAM(7,"CanCam","キャンキャン",28),
    SCAWAII(8,"S Cawaii!","エスカワイイ",38),
    CHEER(9,"CHEER","チア",26),
    CINEMA_CINEMA(10,"Cinema Cinema","シネマシネマ",14),
    CREA(11,"CREA","クレア",39),
    DUET(12,"DUeT","デュエット",2),
    EYE_AI(13,"Eye-Ai","アイアイ",40),
    FINEBOYS(14,"FINEBOYS","ファインボーイズ",25),
    FINEBOYS_BEAUTY(15,"FINEBOYS+plus BEAUTY","ファインボーイズ・ビューティ",25),
    GINGER(16,"GINGER","ジンジャー",16),
    HANAKO(17,"Hanako","ハナコ",9),
    J_GENERATION(18,"J-GENERATION","ジェイ・ジェネレーション",4),
    JJ(19,"JJ","ジェイ・ジェイ",17),
    MINI(20,"mini","ミニ",26),
    MORE(21,"MORE","モア",2),
    MYOJO(22,"Myojo","ミョウジョウ",2),
    MYOJI_LIVE(23,"MyojoLIVE!","ミョージョーライブ",2),
    NONNO(24,"non no","ノンノ",2),
    TV_GUIDE_PERSON(25,"TV ガイド PERSON","テレビガイドパーソン",23),
    PICT_UP(26,"PICT UP","ピクトアップ",30),
    POTATP(27,"POTATO","ポテト",14),
    RAY(28,"Ray","レイ",38),
    SEVENTEEM(29,"Seventeen","セブンティーン",2),
    SPUR(30,"SPUR","シュプール",2),
    STEADY(31,"steady.","ステディ.",26),
    TIPO(32,"Tipo","ティーポ",7),
    TV_LIFE(33,"TV LIFE","テレビライフ",14),
    TV_GUIDE_ALPHA(34,"TVガイドAlpha","テレビガイドアルファ",23),
    SKAPA_TV_GUIDE(35,"スカパー!TVガイドBS+CS","スカパー！TVガイド",23),
    TV_STATION(36,"TVステーション","テレビステーション",6),
    VIVI(37,"ViVi","ヴィヴィ",18),
    VOCE(38,"VOCE","ヴォーチェ",18),
    WINK_UP(39,"Wink up","ウインクアップ",13),
    WITH(40,"with","ウィズ",18),
    THE_TV_SHOW(41,"ザテレビジョンShow","ザテレビジョンショー",1),
    THE_TV_ZOOM(42,"ザテレビジョンZoom!!","ザテレビジョンズーム",1),
    J_MOVIE_MAGAZINE(43,"J Movie Magazine","ジェイムービーマガジン",12),
    CINEMA_SQUARE(44,"CINEMA SQUARE","シネマスクエア",25),
    J_KENKYUKAI(45,"ジャニーズ研究会","ジャニーズケンキュウカイ",4),
    STAGE_SQUARE(46,"STAGE SQUARE","ステージスクエア",25),
    DANCE_SQUARE(47,"Dance SQUARE","ダンススクエア",25),
    CHEESE(48,"Cheese!","チーズ",28),
    NEWSWEEK_JP(49,"Newsweek Japan","ニューズウィーク",31),
    BETSU_KOMI(50,"ベツコミ","ベツコミ",28),
    POPOLO(51,"Popolo","ポポロ",29),
    MINA(52,"mina","ミーナ",32),
    ONGAKU_HITO(53,"音楽と人","オンガクトヒト",33),
    MONTHLY_TV_GUIDE(54,"月刊TVガイド","テレビガイド",23),
    MONTHLY_TV_NAVI(55,"月刊テレビナビ","テレビナビ",5),
    WEEKLY_TV_GUIDE(56,"週刊TVガイド","テレビガイド",23),
    WEEKLY_ASCII(57,"週刊アスキー","アスキー",34),
    WEEKLY_ASAHI(58,"週刊朝日","シュウカンアサヒ",22),
    SOUEN(59,"装苑","ソウエン",35),
    ASAHI_SHOGAKUSEI_NEWS(60,"朝日小学生新聞","アサヒショウガクセイシンブン",37),
    WALKER_TOKAI(61,"東海ウォーカー","東海ウォーカー",1),
    NIKKEI_ENTERTAINMENT(62,"日経エンタテインメント!","ニッケイエンタテイメント",24),
    NIHON_EIGA_NAVI(63,"日本映画navi","ニホンエイガナビ",5),
    BIST(64,"美ST","ビスト",17);

    private final Integer id;
    private final String name;
    private final String kana;
    private final Integer publisherId;


    MagazineEnum(Integer id, String name, String kana, Integer publisherId) {
        this.id = id;
        this.name = name;
        this.kana = kana;
        this.publisherId = publisherId;
    }

    /**
     * IDからenumを取得します。
     *
     * @param id
     * @return
     */
    public static MagazineEnum get(Integer id) {
        return Arrays.stream(MagazineEnum.values()).filter(e -> e.id == (long) id).findFirst().orElse(null);
    }

    /**
     * 引数Stringから雑誌Enumを見つけ、返します
     * TODO：1つだけ一番初めに見つけたやつを返す仕様になっています。複数あった場合とか、配列に入れて返すことができない。
     *
     * @param target
     * @return
     */
    public static MagazineEnum findIdByStringList(String target) {
        MagazineEnum res = null;

        // 文字列を半角に直せるところは直します
        // https://qiita.com/makimaki913/items/df745b85b802099a6e32
        target = Normalizer.normalize(target, Normalizer.Form.NFKC);

        // Enumのnameから見つけます
        for (String s : target.split(" ")) {
            res = Arrays.stream(MagazineEnum.values()).filter(e -> s.equals(e.name) || regexConcat(e.name, s)).findFirst().orElse(null);
            if (res != null) {
                break;
            }
        }

        // Enumのnameから見つけられなかった場合、enumのkanaで探します
        if (res == null) {
            for (String s : target.split(" ")) {
                res = Arrays.stream(MagazineEnum.values()).filter(e -> s.matches(e.kana) || regexConcat(e.kana, s)).findFirst().orElse(null);
                if (res != null) {
                    break;
                }
            }
        }
        return res;
    }

    /**
     * 引数1(enum.name)に半角スペースが含まれる場合、分割してregexに設定。
     * 引数2(target string)が一致するかどうかを返します。
     *
     * @param name
     * @param target
     * @return
     */
    private static boolean regexConcat(String name, String target) {
        if (!name.contains(" ")) {
            return false;
        }

        String[] regexElems = name.split(" ");
        return target.matches(regexElems[0] + ".?" + regexElems[1]);
    }

    /**
     * 引数の文字列から見つけた雑誌名をIDとともに返却します。
     *
     * @param target
     * @return
     */
    public static List<Long> findMagazineIdByText(String target) {
        return  Arrays.stream(MagazineEnum.values()).filter(e -> target.contains(e.name) || regexConcat(e.name, target)).map(e -> (long) e.id).collect(Collectors.toList());
    }

    /**
     * 引数のIDリストから雑誌名をリストにして返却します。
     *
     * @param idList
     * @return
     */
    public static List<String> findMagazineNameList(List<Long> idList) {
        List<String> result = new ArrayList<>();
        for (Long id : idList) {
            MagazineEnum e = MagazineEnum.get(Math.toIntExact(id));
            if (e != null) {
                result.add(e.name);
            }
        }
        return result;
    }
}
