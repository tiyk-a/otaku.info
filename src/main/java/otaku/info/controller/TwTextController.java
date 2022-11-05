package otaku.info.controller;

import org.apache.log4j.Logger;
import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import otaku.info.dto.TwiDto;
import otaku.info.entity.*;
import otaku.info.enums.*;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
import otaku.info.setting.Setting;
import otaku.info.utils.DateUtils;
import otaku.info.utils.ServerUtils;
import otaku.info.utils.StringUtilsMine;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TwTextController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("TwTextController");

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    RakutenController rakutenController;

    @Autowired
    private StationService stationService;

    @Autowired
    private IMService imService;

    @Autowired
    private BlogPostService blogPostService;

    @Autowired
    private PMService pmService;

    @Autowired
    private Setting setting;

    @Autowired
    private StringUtilsMine stringUtilsMine;

    @Autowired
    private ServerUtils serverUtils;

    private final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("M/d");
    private final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd");
    private final DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("hh:mm");
    private final DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("MM/dd HH:mm");
    private final DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 新商品取り込みのTwitterポスト文章
     *
     * @param twiDto
     * @return
     */
    public String twitter(TwiDto twiDto) {

        // memberのタグを作る
        String memTag = "";
        if (twiDto.getMemList() != null && twiDto.getMemList().size() > 1) {
            memTag = String.join(" #", twiDto.getMemList());
        } else if (twiDto.getMemList() != null && twiDto.getMemList().size() == 1) {
            memTag = "#" + twiDto.getMemList().get(0);
        }

        // teamのタグを作る
        String teamTag = "";
        if (twiDto.getTeamNameList() != null && twiDto.getTeamNameList().size() > 1) {
            teamTag = String.join(" #", twiDto.getTeamNameList());
        } else if (twiDto.getTeamNameList() != null && twiDto.getTeamNameList().size() == 1) {
            teamTag = "#" + twiDto.getTeamNameList().get(0);
        }

        // タグをまとめる
        String tags = "";
        if (!teamTag.equals("")) {
            tags = teamTag;
        }

        if (!memTag.equals("")) {
            tags = tags + " " + memTag;
        }

        // URL
        String a_url = "";
        if (!twiDto.getAmazon_url().equals("")) {
            a_url = "\nAmazon：" + twiDto.getAmazon_url();
        }

        String r_url = "";
        if (!twiDto.getRakuten_url().equals("")) {
            r_url = "\n楽天：" + twiDto.getRakuten_url();
        }

        return "新商品の情報です！\n" + twiDto.getTitle() + "\n発売日：" + sdf1.format(twiDto.getPublication_date()) + a_url + r_url + "\n" + tags;
    }

    /**
     * 商品発売予告のツイート文章
     *
     * @param im
     * @param teamId
     * @param itemUrl
     * @param frontFlg フロントからの呼び出しの場合、true->楽天URL検索とかなし
     * @return
     */
    public String futureItemReminder(IM im, Long teamId, String itemUrl, boolean frontFlg) {
        int diff = dateUtils.dateDiff(new Date(), im.getPublication_date()) + 1;
        String tags = "#" + TeamEnum.get(teamId).getMnemonic();

        // メンバー登録があるなら
        if (im.getMemArr() != null && !im.getMemArr().equals("")) {
            List<String> menNameList = StringUtilsMine.stringToLongList(im.getMemArr()).stream().map(e -> MemberEnum.get(e).getName().replaceAll(" ", "")).collect(Collectors.toList());
            String memTag = "";
            // メンバー一人の場合
            if (menNameList.size() <2) {
                memTag = "#" + menNameList.get(0);
            } else {
                // メンバー複数の場合
                memTag = String.join(" #", menNameList);
            }
            // #team + " " + #memTags
            tags = tags + " " + memTag;
        }

        BlogPost blogPost = blogPostService.findByImIdBlogEnumId(im.getIm_id(), TeamEnum.get(teamId).getBlogEnumId()).get(0);
        String title = "";
        String url = "";

        try {
            url = im.getRakuten_url();
            if (url == null || url.equals("") && !frontFlg) {
                url = rakutenController.findRakutenUrl(im.getTitle(), teamId);
            }
        } catch (Exception e) {
            logger.error("TW textエラー");
            e.printStackTrace();
        }

        if (itemUrl != null && !itemUrl.isEmpty() && url.equals("")) {
            url = itemUrl;
        } else if (url.equals("")) {
            url = BlogEnum.get(teamId).getSubDomain() + "blog/" + blogPost.getWp_id();
        }

        if (StringUtils.hasText(im.getTitle())) {
            title = im.getTitle();
        }

        // ノート（フリー記述）
        String note = "";
        if (im.getNote() != null && !im.getNote().equals("")) {
            note = im.getNote();
        }
        return "【発売まで" + diff + "日】\n" + title + "\n" + sdf1.format(im.getPublication_date()) + "発売！" + note + "\n" + tags + "\n" + url;
    }

    /**
     * 本日発売の商品のアナウンス文章を作る
     *
     * @param im
     * @param r_url
     * @return
     */
    public String releasedItemAnnounce(IM im, String r_url) {

        String a_url = "";

        if (im.getAmazon_image() != null && !im.getAmazon_image().equals("")) {
            a_url = StringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse("") + "\n";
        }

        if (!r_url.equals("")) {
            r_url = "楽天：" + r_url + "\n";
        }

        // ノート（フリー記述）
        String note = "";
        if (im.getNote() != null && !im.getNote().equals("")) {
            note = im.getNote();
        }

        String str1 = "本日発売！\n" + im.getTitle() + "" + a_url + r_url + note;

        // twitterタグ、DB使わないで取れてる
        String tags = TeamEnum.findMnemonicListByTeamIdList(StringUtilsMine.stringToLongList(im.getTeamArr())).stream().collect(Collectors.joining(" #","#",""));
        return str1 + "\n" + tags;
    }

    public String twitterPerson(TwiDto twiDto, String memberName) {

        String a_url = "";
        if (!twiDto.getAmazon_url().equals("")) {
            a_url = "Amazon：" + twiDto.getAmazon_url() + "%\n";
        }

        String r_url = "";
        if (!twiDto.getRakuten_url().equals("")) {
            r_url = "楽天：" + twiDto.getRakuten_url() + "%\n";
        }

        String result = memberName + "君の新商品情報です！\n" + twiDto.getTitle() + "\n発売日：" + sdf1.format(twiDto.getPublication_date()) + "\n" + a_url + r_url;
        if (result.length() + memberName.length() < 135) {
            result = memberName + "君の新商品情報です！\n" + twiDto.getTitle() + "\n発売日：" + sdf1.format(twiDto.getPublication_date()) + "\n" + a_url + r_url;
        }
        String tags = String.join("#", twiDto.getTeamNameList());
        return result + "\n" + tags + "\n#" + memberName;
    }

    /**
     * TV出演情報があるグループのTwitter投稿文
     *
     * @param ele Mapから抜き取ったEntry(key=TeamId,value=List<Program>)
     * @param forToday 今日の情報(TRUE)、明日の情報(FALSE)
     * @param date 情報の日付
     * @return
     */
    public String tvPost(Map.Entry<Long, List<PM>> ele, boolean forToday, Date date, Long teamId) {
        String dateStr = forToday ? "今日(" + sdf2.format(date) + ")" : "明日(" + sdf2.format(date) + ")";
        String teamName = TeamEnum.get(teamId).getName();
        String result= "";
        if (!teamName.equals("")) {
            result = dateStr + "の" + teamName + "のTV出演情報です。\n";
        } else {
            result = dateStr + "のTV出演情報です。\n";
        }

        String info = "";
        for (PM pm : ele.getValue()) {
            String stationNameList = "";
            if (pm.getStationArr() != null || !pm.getStationArr().equals("")) {
                List<Long> stationIdList = StringUtilsMine.stringToLongList(pm.getStationArr());
                stationNameList = "";
                for (Long stationId : stationIdList) {
                    String stationName = stationService.getStationNameByEnumDB(stationId);
                    if (!stationName.equals("")) {
                        if (stationNameList.equals("")) {
                            stationNameList = "(";
                        }
                        stationNameList = stationNameList + ", " + stationName;
                    }
                }

                if (!stationNameList.equals("")) {
                    stationNameList = stationNameList + ")";
                }
            }
                info = info + dtf1.format(pm.getOn_air_date()) + " " + pm.getTitle() + stationNameList + "\n";
        }

        // blogへの誘導
        BlogEnum blogEnum = BlogEnum.get(TeamEnum.get(teamId).getBlogEnumId());
        String blog = "一覧はこちら\n" + blogEnum.getSubDomain() + "pages/" + blogEnum.getTvPageId();
        return result + info + blog;
    }

    /**
     * 直近のTV番組1件のアラート文章を作ります。
     * このあと06/13 05:25〜めざましテレビ[デ](フジテレビ)に、西畑大吾くんが出演します。#西畑大吾 #なにわ男子
     *
     * <MM/dd HH:mm>から<TITLE>に、<MEMBER || TEAM>が出演します。\n<CHANNEL>\n#<MEMBER || TEAM>
     * 1 ver1つ投稿する
     * ついでに発売前商品とかのIMリンクを表示
     *
     * @param pm
     * @return Map<TeamId, text>
     */
    public String tvAlert(PM pm) {
        String result = "";

        // チームとメンバーは2つ用意が必要
        // ①メンバ（正式） or チーム（正式）（メンバー名があるならチーム名は入れない）
        // ②チーム(tag)　チーム(tag) メンバ(tag)　メンバ(tag)
        List<TeamEnum> teamEnumList = StringUtilsMine.stringToLongList(pm.getTeamArr()).stream().map(TeamEnum::get).collect(Collectors.toList());
        List<MemberEnum> memberEnumList = StringUtilsMine.stringToLongList(pm.getMemArr()).stream().map(MemberEnum::get).collect(Collectors.toList());

        Map<TeamEnum, List<MemberEnum>> groupEnum = serverUtils.groupMem(teamEnumList, memberEnumList);

        // ①を入れる
        String teamAndMemCont = "";
        // ②を入れる→タグにする
        List<String> tagList = new ArrayList<>();
        List<String> tagListTeam = new ArrayList<>();
        List<String> tagListMem = new ArrayList<>();

        for (Map.Entry<TeamEnum, List<MemberEnum>> elem : groupEnum.entrySet()) {
            // ②に入れる
            tagListTeam.add(elem.getKey().getMnemonic());

            // メンバーがある場合
            List<MemberEnum> memberList = elem.getValue();
            // ①にそのチームのメンバーを全部入れる
            List<String> tmpMemNameList = new ArrayList<>();
            if (memberList != null && memberList.size() > 0) {
                for (MemberEnum memberEnum : memberList) {
                    // ②に入れる
                    tagListMem.add(memberEnum.getName());
                    tmpMemNameList.add(memberEnum.getName());
                }
            }

            // そのチームのメンバーがあるようなら①にメンバーを入れる
            if (tmpMemNameList.size() > 0) {
                for (String tmpMemName : tmpMemNameList) {
                    if (!teamAndMemCont.equals("")) {
                        teamAndMemCont = teamAndMemCont + "、";
                    }
                    teamAndMemCont = teamAndMemCont + tmpMemName + "くん";
                }
            } else {
                // そのチームのメンバーがないようなら①にチームを入れる
                if (!teamAndMemCont.equals("")) {
                    teamAndMemCont = teamAndMemCont + "、";
                }
                teamAndMemCont = teamAndMemCont + elem.getKey().getName();
            }
        }

        tagList.addAll(tagListTeam);
        tagList.addAll(tagListMem);

        if (tagList.size() > 0) {
            for (String tag : tagList) {
                if (tag.contains(" ")) {
                    tagList.remove(tag);
                    String removedSpaceName = tag.replaceAll(" ", "");
                    tagList.add(removedSpaceName);
                }
            }
        }

        // Format LocalDateTime
        String formattedDateTime = pm.getOn_air_date().format(dtf2);

        String stationNameList = "";
        if (pm.getStationArr() != null || !pm.getStationArr().equals("")) {
            List<Long> stationIdList = StringUtilsMine.stringToLongList(pm.getStationArr());
            for (Long stationId : stationIdList) {
                String stationName = stationService.getStationNameByEnumDB(stationId);
                if (!stationName.equals("")) {
                    if (!stationNameList.equals("")) {
                        stationNameList = stationNameList + ", ";
                    }
                    stationNameList = stationNameList + stationName;
                }
            }

            if (!stationNameList.equals("")) {
                stationNameList = stationNameList + "です！";
            }
        }

        result = "このあと" + formattedDateTime + "から『" + pm.getTitle() + "』に"
            + teamAndMemCont + "が出演します。"
            + stationNameList
            + "\n" + tagList.stream().collect(Collectors.joining(" #","#",""));
        return result;
    }

    /**
     * 引数チームIDのお勧め商品のテキストを作る
     *
     * @param teamId
     * @return
     */
    public String createRecomItemText(Long teamId) {
        String res = "";
        IM im = imService.findUpcomingImWithUrls(teamId).orElse(null);

        if (im != null) {
            if (im.getAmazon_image() != null && !im.getAmazon_image().equals("")) {
                String tmp = StringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse("");
                String a_url = "";
                if (!tmp.equals("")) {
                    a_url = "Amazon:" + tmp;
                }

                if (!a_url.equals("")) {
                    res = "\n" + sdf2.format(im.getPublication_date()) + "発売の" + im.getTitle() + "は入手済みですか？\n" + a_url;
                }
            }
        }
        return res;
    }

    /**
     * レーベンシュタイン距離で文字列の類似度を判定
     * @param s1
     * @param s2
     * https://qiita.com/hakozaki/items/856230d3f8e29d3302d6
     * @return
     */
    public int getSimilarScoreByLevenshteinDistance(String s1, String s2) {

        // 入力チェックは割愛
        LevensteinDistance dis =  new LevensteinDistance();
        return (int) (dis.getDistance(s1, s2) * 100);
    }

    /**
     * ジャロ・ウィンクラー距離で文字列の類似度を判定
     * @param s1
     * @param s2
     * https://qiita.com/hakozaki/items/856230d3f8e29d3302d6
     * @return
     */
    public int getSimilarScoreByJaroWinklerDistance(String s1, String s2) {

        // 入力チェックは割愛
        JaroWinklerDistance dis =  new JaroWinklerDistance();
        return (int) (dis.getDistance(s1, s2) * 100);
    }
}
