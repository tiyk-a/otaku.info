package otaku.info.enums;

import lombok.Getter;
import otaku.info.entity.Team;

import java.util.Arrays;

@Getter
public enum TeamEnum {

    SNOWMAN(6,null,"","","Snow man","snowmanhayainfo"),
    KANJANI8(7,null,"カンジャニエイト","関ジャニ","関ジャニ∞",""),
    SEXYZONE(8,null,"セクシーゾーン","セクゾ","Sexy Zone","sexyz0neinfo"),
    TOKIO(9,null,"トキオ","","TOKIO",""),
    V6(10,null,"ブイシックス","","V6",""),
    ARASHI(11,null,"アラシ","嵐","ARASHI",""),
    NEWS(12,null,"ニュース","","NEWS Johnny's",""),
    KISMYFT2(13,null,"キスマイフットツー","キスマイ","Kis-My-Ft2",""),
    ABCZ(14,null,"エービーシーズィー","エビ","A.B.C-Z",""),
    JOHNNYSWEST(15,null,"ジャニーズウェスト","ジャニスト ","ジャニーズWEST ",""),
    KINGPRINCE(16,null,"キングアンドプリンス ","キンプリ ","King & Prince ","princehayainfo"),
    SIXTONES(17,null,"ストーンズ ","ストンズ ","SixTONES ","sixtoneshayain1"),
    NANIWADANSHI(18,null,"ナニワダンシ ","なにわ ","なにわ男子 ","naniwa_hayainfo"),
    HEYSAYJUMP(19,null,"ヘイセイジャンプ ","JUMP ","Hey! Say! JUMP ",""),
    KATTUN(20,null,"カトゥーン ","KAT-TUN ","KAT-TUN ",""),
    KINKIKIDS(21,null,"キンキキッズ ","キンキ ","KinKi Kids ","");

    private Integer id;
    private String anniversary;
    private String kana;
    private String mnemonic;
    private String name;
    private String tw_id;

    TeamEnum(Integer id, String anniversary, String kana, String mnemonic, String name, String tw_id) {
        this.id = id;
        this.anniversary = anniversary;
        this.kana = kana;
        this.mnemonic = mnemonic;
        this.name = name;
        this.tw_id = tw_id;
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
}
