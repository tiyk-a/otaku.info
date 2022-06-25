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

import java.text.ParseException;
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
    LineController lineController;

    @Autowired
    RakutenController rakutenController;

    @Autowired
    private TeamService teamService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StationService stationService;

    @Autowired
    private IMService imService;

    @Autowired
    private IMRelService iMRelService;

    @Autowired
    private IMRelMemService imRelMemService;

    @Autowired
    private PRelService pRelService;

    @Autowired
    private PMService pmService;

    @Autowired
    private PMRelService pmRelService;

    @Autowired
    private PRelMemService pRelMemService;

    @Autowired
    private PMRelMemService pmRelMemService;

    @Autowired
    private PmVerService pmVerService;

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

        String tags = "#" + TeamEnum.get(twiDto.getTeam_id()).getMnemonic() + memTag;
        return "新商品の情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl() + "%0A" + tags;
    }

    public String futureItemReminder(IM im, Long teamId, String itemUrl, List<Long> memIdList) {
        int diff = dateUtils.dateDiff(new Date(), im.getPublication_date()) + 1;
        String tags = "#" + TeamEnum.get(teamId).getMnemonic();

        if (memIdList != null && memIdList.size() > 0) {
            tags = tags + String.join(" #" + memIdList);
        }

        String title = "";
        String url = "";
        IMRel rel = null;
        List<IMRel> tmpList = iMRelService.findByImIdTeamId(im.getIm_id(), teamId);
        if (!tmpList.isEmpty()) {
            rel = tmpList.get(0);
        }

        try {
            url = rakutenController.findRakutenUrl(im.getTitle(), teamId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (itemUrl != null && !itemUrl.isEmpty() && url.equals("")) {
            url = itemUrl;
        } else if (url.equals("")) {
            url = BlogEnum.get(teamId).getSubDomain() + "blog/" + rel.getWp_id();
        }

        if (StringUtils.hasText(im.getTitle())) {
            title = im.getTitle();
        }

        return "【発売まで" + diff + "日】%0A%0A" + title + "%0A発売日：" + sdf1.format(im.getPublication_date()) +  "%0A%0A" + url + "%0A" + tags;
    }

    /**
     * 本日発売の商品のアナウンス文章を作る
     *
     * @param im
     * @param teamIdList
     * @param imageUrl
     * @return
     */
    public String releasedItemAnnounce(IM im, List<Long> teamIdList, String imageUrl) {

        String url = "";

        if (im.getAmazon_image() != null) {
            url = stringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse(imageUrl);
        } else {
            url = imageUrl;
        }

        String str1 = "本日発売！%0A%0A" + im.getTitle() + "%0A" + url;
        // twitterタグ、DB使わないで取れてる
        String tags = TeamEnum.findMnemonicListByTeamIdList(teamIdList).stream().collect(Collectors.joining(" #","#",""));
        return str1 + "%0A" + tags;
    }

    public String twitterPerson(TwiDto twiDto, String memberName) {
        String result = memberName + "君の新商品情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl();
        if (result.length() + memberName.length() < 135) {
            result = memberName + "君の新商品情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A#" + memberName + "%0A#" + twiDto.getUrl();
        }
        String tags = "#" + TeamEnum.get(twiDto.getTeam_id()).getMnemonic();
        return result + "%0A%0A" + tags;
    }

    /**
     * TV出演情報があるグループのTwitter投稿文
     *
     * @param ele Mapから抜き取ったEntry(key=TeamId,value=List<Program>)
     * @param forToday 今日の情報(TRUE)、明日の情報(FALSE)
     * @param date 情報の日付
     * @return
     */
    public String tvPost(Map.Entry<Long, List<PMVer>> ele, boolean forToday, Date date, Long teamId) {
        String dateStr = forToday ? "今日(" + sdf2.format(date) + ")" : "明日(" + sdf2.format(date) + ")";
        String teamName = teamService.getTeamName(ele.getKey());
        String result= "";
        if (!teamName.equals("")) {
            result = dateStr + "の" + teamName + "のTV出演情報です。%0A%0A";
        } else {
            result = dateStr + "のTV出演情報です。%0A%0A";
        }

        String info = "";
        for (PMVer p : ele.getValue()) {
            PM pm = pmService.findByPmId(p.getPm_id());
            info = info + dtf1.format(p.getOn_air_date()) + " " + pm.getTitle() + " (" + stationService.getStationNameByEnumDB(p.getStation_id()) + ")%0A";
        }

        // blogへの誘導
        BlogEnum blogEnum = BlogEnum.get(TeamEnum.get(teamId).getBlogEnumId());
        String blog = "一覧はこちら%0A" + blogEnum.getSubDomain() + "pages/" + blogEnum.getTvPageId();
        return result + info + blog;
    }

    /**
     * 直近のTV番組1件のアラート文章を作ります。
     * このあと06/13 05:25〜めざましテレビ[デ](フジテレビ)に、なにわ男子が出演します。ぜひご覧ください！#なにわ男子
     *
     * <MM/dd HH:mm>から<TITLE>に、<MEMBER || TEAM>が出演します。\n<CHANNEL>\nぜひご覧ください！#<MEMBER || TEAM>
     * 1 ver1つ投稿する
     *
     * @param ver
     * @return Map<TeamId, text>
     */
    public Map<Long, String> tvAlert(PMVer ver) {
        // teamId, null
        Map<Long, String> resultMap = new HashMap<>();
        PM pm = pmService.findByPmId(ver.getPm_id());
        List<PMRel> relList = pmRelService.findByPmIdDelFlg(ver.getPm_id(), false);
        List<Long> relIdList = relList.stream().map(PMRel::getPm_rel_id).collect(Collectors.toList());
        List<PMRelMem> relMemList = pmRelMemService.findByPmRelIdListDelFlg(relIdList, false);
        List<Long> teamIdList = relList.stream().map(PMRel::getTeam_id).collect(Collectors.toList());

        // 返却するMapにKey(PmId)のみ詰め込みます。
        if (teamIdList.size() > 0) {
            // Mapのkeyを作り格納
            teamIdList.forEach(e -> resultMap.put(e, null));
            // TODO:ここでnullになる
            for (Long teamId : teamIdList) {
                List<String> tagList = new ArrayList<>();
                String teamName = TeamEnum.get(teamId).getName();
                String mnemonic = TeamEnum.get(teamId).getMnemonic();
                tagList.add(mnemonic);

                String result = "";

                // Format LocalDateTime
                String formattedDateTime = ver.getOn_air_date().format(dtf2);

                // Member情報のあるTeamの場合/ないTeamの場合で文章とタグが異なります。
                String memberName = "";
                if (relList.size() > 0) {
                    for (PMRel rel : relList) {
                        List<String> tmpNameList = relMemList.stream()
                                .filter(e -> e.getPm_rel_id().equals(rel.getPm_rel_id()))
                                .map(e -> MemberEnum.get(e.getMember_id()).getName())
                                .collect(Collectors.toList());
                        List<String> tmpMnemonicList = relMemList.stream()
                                .filter(e -> e.getPm_rel_id().equals(rel.getPm_rel_id()))
                                .map(e -> MemberEnum.get(e.getMember_id()).getMnemonic())
                                .collect(Collectors.toList());

                        if (tmpNameList.size() > 0) {
                            // 文章用
                            memberName = String.join("・", tmpNameList);
                        }

                        if (tmpMnemonicList.size() > 0) {
                            // タグ用
                            tagList.addAll(tmpMnemonicList);
                        }
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
                }

                result = "このあと" + formattedDateTime + "から『" + pm.getTitle() + "』に"
                        + (memberName.equals("") ? teamName : memberName) + "が出演します。"
                        + "\n\nチャンネル：" + stationService.findById(ver.getStation_id()).getStation_name()
                        + "\nぜひご覧ください！\n" + "%0A%0A" + tagList.stream().collect(Collectors.joining(" #","#",""));
                resultMap.put(teamId, result);
            }
        }
        return resultMap;
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

    /**
     * TV番組固定ページのテキストを作成。
     * 1つのドメインにポストするProgramリストが日にちごちゃ混ぜで入ってくる
     *
     * @param pmList
     * @return
     */
    public String tvPageText(List<PMVer> pmList, String subDomain) throws ParseException {
        if (pmList.size() == 0) {
            return "";
        }

        String result = "[toc depth='6']";
        // 丁寧にプログラムをソートする（放送日、チーム）
        // 1:日付ごとにまとめる<DateStr, List<Program>>
        Map<String, List<PMVer>> datePMap = new TreeMap<>();
        for (PMVer p : pmList) {

            String targetDate = dtf3.format(p.getOn_air_date());
            List<PMVer> tmpList;
            if (!datePMap.containsKey(targetDate)) {
                tmpList = new ArrayList<>();
                tmpList.add(p);
                datePMap.put(targetDate, tmpList);
            } else {
                tmpList = datePMap.get(targetDate);
            }
            tmpList.add(p);
            datePMap.put(targetDate, tmpList);
        }

        // 2: 日付でまとまったMapの中身を時間で並べ替える
        // 放送局だけの異なる番組をまとめたい<DateStr, Map<title, List<Program>>>
        Map<String, Map<String, List<PMVer>>> gatheredMap = new TreeMap<>();
        if (datePMap.size() > 0) {
            for (Map.Entry<String, List<PMVer>> e : datePMap.entrySet()) {
                Map<String, List<PMVer>> tmpMap = new TreeMap<>();
                List<PMVer> tmpList;
                for (PMVer p : e.getValue()) {
                    PM pm = pmService.findByPmId(p.getPm_id());
                    if (tmpMap.containsKey(pm.getTitle())) {
                        tmpList = tmpMap.get(pm.getTitle());
                        if (!tmpList.get(0).getOn_air_date().equals(p.getOn_air_date())) {
                            tmpList = new ArrayList<>();
                        }
                    } else {
                        tmpList = new ArrayList<>();
                    }
                    tmpList.add(p);
                    tmpMap.put(pm.getTitle(), tmpList);
                }
                gatheredMap.put(e.getKey(), tmpMap);
            }
        }

        // ソートされた日付ごとのマップができたので、それぞれの日の文章を作成する
        // 結果をまとめるリスト<h2, 番組ごとのテキストのリスト>
        Map<String, List<String>> textByDays = new TreeMap<>();
        List<String> textList = new ArrayList<>();
        String h2 = "";
        for (Map.Entry<String, Map<String, List<PMVer>>> e : gatheredMap.entrySet()) {

            // 総合ブログの場合チーム名の取得とかが必要
            String tmp = "";
            for (Map.Entry<String, List<PMVer>> p : e.getValue().entrySet()) {
                PMVer masterP = p.getValue().get(0);

                String teamName = "";
                if (subDomain.equals("NA")) {
                    List<Long> pTeamIdList = pRelService.getTeamIdList(masterP.getPm_id());
                    if (pTeamIdList != null && !pTeamIdList.isEmpty() && !pTeamIdList.get(0).equals(0L)) {
                        List<String> teamNameList = TeamEnum.findTeamNameListByTeamIdList(pTeamIdList);
                        teamName = String.join("/", teamNameList);
                    }
                }

                String memberName = "";
                List<Long> memberIdList = pRelService.getMemberIdList(masterP.getPm_id());
                if (memberIdList != null && !memberIdList.isEmpty() && memberIdList.get(0) != null && !memberIdList.get(0).equals(0L)) {

                    List<String> memberNameList = MemberEnum.findMNameListByIdList(memberIdList);
                    memberName = String.join("/", memberNameList);
                }

                PM pm = pmService.findByPmId(masterP.getPm_id());
                String description = StringUtils.hasText(pm.getDescription()) ? pm.getDescription() : "";
                tmp = tmp + "</br ><h6>" + dtf1.format(masterP.getOn_air_date()) + ":　" + teamName + " " + memberName + "：" + pm.getTitle() + "</h6><br /><p>番組概要：" + description + "</p>";

                String broad = "<p>放送局：";
                for (PMVer r : p.getValue()) {
                    String stationName = stationService.getStationNameByEnumDB(r.getStation_id());
                    broad = broad + stationName + "<br />";
                }
                broad = broad + "</p>";
                tmp = tmp + broad;
                textList.add(tmp);
                tmp = "";
            }

            // リストの最後の要素の場合、h2を用意、マップに要素を追加、使い回すリストとh2を空にする
            // h2用意
            String date = e.getKey().replaceAll("^\\d{4}", "");
            date = date.substring(0,2) + "/" + date.substring(2, 4);
            String day = dateUtils.getDay(e.getKey());
            h2 = "<h2>" + date + "(" +  day + ")</h2>\n";
            textByDays.put(h2, textList);
            h2 = "";
            textList = new ArrayList<>();
        }

        // ループ終わったら日毎のリストをまとめる
        List<String> htmlList = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : textByDays.entrySet()) {
            htmlList.add(String.join("\n", e.getKey(), String.join("\n", e.getValue())));
        }

        if (htmlList.size() > 0) {
            result = String.join("\n", result, String.join("\n", htmlList));
        }
        return result;
    }
}
