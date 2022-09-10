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

        String memTag = "";
        if (twiDto.getMemList() != null && twiDto.getMemList().size() > 0) {
            memTag = String.join(" #", twiDto.getMemList());
        }

        String tags = String.join("#", twiDto.getTeamNameList()) + " " + memTag;

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

    public String futureItemReminder(IM im, Long teamId, String itemUrl) {
        int diff = dateUtils.dateDiff(new Date(), im.getPublication_date()) + 1;
        String tags = "#" + TeamEnum.get(teamId).getName();

        if (im.getMemArr() != null && !im.getMemArr().equals("")) {
            List<String> menNameList = StringUtilsMine.stringToLongList(im.getMemArr()).stream().map(e -> MemberEnum.get(e).getName().replaceAll(" ", "")).collect(Collectors.toList());
            tags = tags + String.join(" #" + menNameList);
        }

        BlogPost blogPost = blogPostService.findByImIdBlogEnumId(im.getIm_id(), TeamEnum.get(teamId).getBlogEnumId());
        String title = "";
        String url = "";

        try {
            url = rakutenController.findRakutenUrl(im.getTitle(), teamId);
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

        return "【発売まで" + diff + "日】\n" + title + "\n発売日：" + sdf1.format(im.getPublication_date()) + "\n" + tags + "\n" + url;
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

        String str1 = "本日発売！\n" + im.getTitle() + "" + a_url + r_url;

        // twitterタグ、DB使わないで取れてる
        String tags = TeamEnum.findMnemonicListByTeamIdList(StringUtilsMine.stringToLongList(im.getTeamArr())).stream().collect(Collectors.joining(" #","#",""));
        return str1 + "%\n" + tags;
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
                stationNameList = "(";
                for (Long stationId : stationIdList) {
                    stationNameList = stationNameList + ", " + stationService.getStationNameByEnumDB(stationId);
                }
                stationNameList = stationNameList + ")";
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
     * このあと06/13 05:25〜めざましテレビ[デ](フジテレビ)に、なにわ男子が出演します。ぜひご覧ください！#なにわ男子
     *
     * <MM/dd HH:mm>から<TITLE>に、<MEMBER || TEAM>が出演します。\n<CHANNEL>\nぜひご覧ください！#<MEMBER || TEAM>
     * 1 ver1つ投稿する
     * ついでに発売前商品とかのIMリンクを表示
     *
     * @param pm
     * @return Map<TeamId, text>
     */
    public String tvAlert(PM pm) {
        String result = "";

        List<Long> teamIdList = StringUtilsMine.stringToLongList(pm.getTeamArr());
        List<String> tagList = new ArrayList<>();

        String teamName = "";
        for (Long teamId : teamIdList) {
            // チーム名をセット
            String tmp = TeamEnum.get(teamId).getName();
            if (teamName.equals("")) {
                teamName = tmp;
            } else {
                teamName = teamName + "、" + tmp;
            }

            // タグにチーム名をセット
            tagList.add(tmp);
        }

        List<Long> memIdList = StringUtilsMine.stringToLongList(pm.getMemArr());

        String memName = "";
        for (Long memId : memIdList) {
            // mem名をセット
            String tmp = MemberEnum.get(memId).getName();
            if (memName.equals("")) {
                memName = tmp;
            } else {
                memName = memName + "、" + tmp;
            }

            // タグにメンバー名をセット
            tagList.add(MemberEnum.get(memId).getName());
        }

        // team&mem名を合わせる
        String names = "";
        if (!teamName.equals("") && !memName.equals("")) {
            names = teamName + "、" + memName;
        } else if (!teamName.equals("") && memName.equals("")) {
            names = teamName;
        } else if (teamName.equals("") && !memName.equals("")) {
            names = memName;
        }

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
            stationNameList = "チャンネル：";
            for (Long stationId : stationIdList) {
                stationNameList = stationNameList + ", " + stationService.getStationNameByEnumDB(stationId);
            }
        }

        IM im = imService.findUpcomingImWithUrls(teamIdList.get(0)).orElse(null);

        String a_url = "";
        if (im != null && im.getAmazon_image() != null && !im.getAmazon_image().equals("")) {
            a_url = "Amazon:" + StringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse("");
        }

        if (im != null) {
            result = "このあと" + formattedDateTime + "から『" + pm.getTitle() + "』に"
                + names + "が出演します。"
                + stationNameList
                + "\n" + tagList.stream().collect(Collectors.joining(" #","#",""))
                + "\n" + sdf2.format(im.getPublication_date()) + "発売の" + im.getTitle() + "は入手済みですか？\n" + a_url;
        }
        return result;
    }

    public String tvAlert2(PM pm) {
        String result = "";

        List<Long> teamIdList = StringUtilsMine.stringToLongList(pm.getTeamArr());
        List<String> tagList = new ArrayList<>();

        String teamName = "";
        for (Long teamId : teamIdList) {
            // チーム名をセット
            String tmp = TeamEnum.get(teamId).getName();
            if (teamName.equals("")) {
                teamName = tmp;
            } else {
                teamName = teamName + "、" + tmp;
            }

            // タグにチーム名をセット
            tagList.add(tmp);
        }

        List<Long> memIdList = StringUtilsMine.stringToLongList(pm.getMemArr());

        String memName = "";
        for (Long memId : memIdList) {
            // mem名をセット
            String tmp = MemberEnum.get(memId).getName();
            if (memName.equals("")) {
                memName = tmp;
            } else {
                memName = memName + "、" + tmp;
            }

            // タグにメンバー名をセット
            tagList.add(MemberEnum.get(memId).getName());
        }

        // team&mem名を合わせる
        String names = "";
        if (!teamName.equals("") && !memName.equals("")) {
            names = teamName + "、" + memName;
        } else if (!teamName.equals("") && memName.equals("")) {
            names = teamName;
        } else if (teamName.equals("") && !memName.equals("")) {
            names = memName;
        }

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
            stationNameList = "チャンネル：";
            for (Long stationId : stationIdList) {
                stationNameList = stationNameList + ", " + stationService.getStationNameByEnumDB(stationId);
            }
        }

        IM im = imService.findUpcomingImWithUrls(teamIdList.get(0)).orElse(null);

        String a_url = "";
        if (im != null && im.getAmazon_image() != null && !im.getAmazon_image().equals("")) {
            a_url = "Amazon:" + StringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse("");
        }

        if (im != null) {
            result = "このあと" + formattedDateTime + "から『" + pm.getTitle() + "』に"
                    + names + "が出演します。"
                    + stationNameList
                    + "\n" + tagList.stream().collect(Collectors.joining(" #","#",""))
                    + "\n" + sdf2.format(im.getPublication_date()) + "発売の" + im.getTitle() + "は入手済みですか？\n" + a_url;
        }
        return result;
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
