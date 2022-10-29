package otaku.info.controller;

import org.apache.log4j.Logger;
import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 投稿用のテキストを色々と生成します。
 *
 */
@Controller
public class TextController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("TextController");

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    AnalyzeController analyzeController;

    @Autowired
    private ItemService itemService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StationService stationService;

    @Autowired
    private IMService imService;

    @Autowired
    private ImVerService imVerService;

    @Autowired
    private PMService pmService;

    @Autowired
    private Setting setting;

    private final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日(E)");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("M/d");
    private final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd");
    private final DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("hh:mm");
    private final DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("MM/dd HH:mm");
    private final DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyyMMdd");

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
        String teamName = TeamEnum.get(ele.getKey()).getName();
        String result= "";
        if (!teamName.equals("")) {
            result = dateStr + "の" + teamName + "のTV出演情報です。%0A%0A";
        } else {
            result = dateStr + "のTV出演情報です。%0A%0A";
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

            info = info + dtf1.format(pm.getOn_air_date()) + " " + pm.getTitle() + stationNameList + "%0A";
        }

        // blogへの誘導
        BlogEnum blogEnum = BlogEnum.get(TeamEnum.get(teamId).getBlogEnumId());
        String blog = "一覧はこちら%0A" + blogEnum.getSubDomain() + "pages/" + blogEnum.getTvPageId();
        return result + info + blog;
    }

    /**
     * WordPressブログのリリース情報固定ページ表示用のテキストを作成
     *
     * @param todayImList
     * @param futureImList
     * @param subDomain
     * @return
     */
    public String blogUpdateReleaseItems(List<IM> todayImList, List<IM> futureImList, String subDomain) {

        String imagePath = subDomain + BlogEnum.findBySubdomain(subDomain).getScheduleImagePath();

        String result = "[toc depth='4']";

        // 今日の/先1週間の商品ごとの文章を作る(List<商品のテキスト>)
        String todaysElems = todayImList.size() > 0 ? blogReleaseItemsText(todayImList, imagePath) : null;
        String futureElems = futureImList.size() > 0 ? blogReleaseItemsText(futureImList, imagePath) : null;

        // 本日発売の商品
        if (todaysElems != null) {
            result = result + "\n" + todaysElems;
        } else {
            result = result + "\n" + "<h2>今日発売の商品はありません。</h2>";
        }

        // 明日以降発売の商品
        String result2 = "";
        if (futureElems != null) {
            result2 = String.join("\n\n", result, futureElems);
        } else {
            result2 = String.join("\n\n", result, "<h2>明日以降1週間内発売の商品はありません。</h2>");
        }

        // テキストは返却
        return result2;
    }

    /**
     * 商品ブログ投稿文章
     * SEO対策のためにトップに自分で作った画像を入れる
     *
     * @param imList
     * @param imagePath
     * @return
     */
    public String blogReleaseItemsText(List<IM> imList, String imagePath) {
        String result = "";

        logger.debug("imList.size=" + imList.size());
        // マスター商品ごとにテキストを作り返却リストに入れる(Itemリストのサイズが0以上のマスタ商品をタイトルでソート)。
        for (IM im : imList) {

            String date = dateUtils.getDay(im.getPublication_date());
            String publicationDate = sdf1.format(im.getPublication_date()) + "(" + date + ")";

            List<ImVer> verList = imVerService.findByImId(im.getIm_id());

            List<String> teamNameList = StringUtilsMine.stringToLongList(im.getTeamArr()).stream().map(e -> TeamEnum.get(e).getName()).collect(Collectors.toList());
            String teamNameUnited = String.join(" ", teamNameList);
            List<Long> memList = StringUtilsMine.stringToLongList(im.getMemArr());

            // h2で表示したい商品のタイトルを生成
            String h2 = "";

            if (memList != null && memList.size() > 0) {
                // メンバー名もある場合はこちら
                List<String> memNameList = memList.stream().map(e -> MemberEnum.get(e).getName()).collect(Collectors.toList());
                h2 = String.join(" ", publicationDate, teamNameUnited, String.join(" ", memNameList), im.getTitle());
            } else {
                // メンバー名ない場合はこちら
                h2 = String.join(" ", publicationDate, teamNameUnited, im.getTitle());
            }

            // htmlタグ付与
            h2 = "<h2 id=id_" + im.getIm_id() + ">" + h2 + "</h2>";

            // SEO対策のため画像を入れる
            String seoImage = "";
            if (imagePath == null) {
                seoImage = "<figure class='wp-block-image size-large'><img src='***INNER_IMAGE***' alt='' class='wp-image-6736'/></figure>";
            } else {
//                seoImage = "PARAM_IMAGE";
                seoImage = imagePath;
            }

            List<String> verTxtList = new ArrayList<>();
            String txt = "";
            if (verList.size() > 0) {
                for (ImVer ver : verList) {
                    txt = "<h3>" + ver.getVer_name() + "</h3>\n" + "[rakuten search='" + im.getTitle() + " " + ver.getVer_name() + "' kw='" + im.getTitle() + " " + ver.getVer_name() + "' amazon=1 rakuten=1 yahoo=1]";
                    verTxtList.add(txt);
                }
            } else {
                txt = "<h3>" + im.getTitle() + "</h3>\n" + "[rakuten search='" + im.getTitle() + "' kw='" + im.getTitle() + "' amazon=1 rakuten=1 yahoo=1]";
                verTxtList.add(txt);
            }

            String pubDate = sdf1.format(im.getPublication_date());
            String publicationDateStr = "<h3>発売日は" + pubDate + "</h3>";

            // SEO対策：external linkとしてグループの公式サイトのリンク
            String seoLink = "";
            for (String teamName : teamNameList) {

                // internalリンクを作る
                TeamEnum teamEnum = TeamEnum.get(teamName);
                String internalTop = teamEnum.getInternalTop();
                if (internalTop == null || internalTop.equals("")) {
                    internalTop = "<p>" + teamName + "の情報まとめは<a href='" + BlogEnum.get(teamEnum.getBlogEnumId()).getSubDomain() + "'>こちら</a></p>";
                } else {
                    internalTop = "<p>" + teamName + "の情報まとめは<a href='" + internalTop + "'>こちら</a></p>";
                }

                // externalリンクを作る。末尾にinternalリンクをつけてあげる
                String tmpLink = TeamEnum.get(teamName).getOfficialSite();
                tmpLink = "<h2>" + teamName + "公式サイト</h2><p>" + teamName + "の公式サイトは<a href='" + tmpLink + "'>こちら</a></p>" + internalTop;
                if (seoLink.isBlank()) {
                    seoLink = tmpLink;
                } else {
                    String tmp = seoLink;
                    seoLink = tmp + tmpLink;
                }
            }

            String text = String.join("\n", h2, publicationDateStr, seoImage, String.join("\n", verTxtList), seoLink);
            // 返却に追加
            result = result + "\n" + text;
        }

        // SEO対策のため画像を入れる(第二引数がある場合)
//        if (imagePath != null) {
//            result.replaceAll("PARAM_IMAGE", imagePath);
//        } else {
//            // 引数に画像ない時も仕方ないからパラメータをから文字で置き換えてあげる
//            result.replaceAll("PARAM_IMAGE", "");
//        }

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

    /**
     * ブログタイトルを作成します。
     * IMからの発売日とタイトルを引数に想定
     *
     * @param publicationDate
     * @param title
     * @return
     */
    public String createBlogTitle(Date publicationDate, String title) {
        if (publicationDate == null) {
            return "2021年XX月XX日" + title;
        }
        return  sdf1.format(publicationDate) + " " + title;
    }

    /**
     * TV番組固定ページのテキストを作成。
     * 1つのドメインにポストするProgramリストが日にちごちゃ混ぜで入ってくる
     *
     * @param pmList
     * @return
     */
    public String tvPageText(List<PM> pmList, String subDomain) throws ParseException {
        if (pmList.size() == 0) {
            return "";
        }

        String result = "[toc depth='4']";
        // 丁寧にプログラムをソートする（放送日、チーム）
        // 1:日付ごとにまとめる<DateStr, List<Program>>
        Map<String, List<PM>> datePMap = new TreeMap<>();
        for (PM p : pmList) {

            String targetDate = dtf3.format(p.getOn_air_date());
            List<PM> tmpList;
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
        Map<String, Map<String, List<PM>>> gatheredMap = new TreeMap<>();
        if (datePMap.size() > 0) {
            for (Map.Entry<String, List<PM>> e : datePMap.entrySet()) {
                Map<String, List<PM>> tmpMap = new TreeMap<>();
                List<PM> tmpList;
                for (PM pm : e.getValue()) {
                    if (tmpMap.containsKey(pm.getTitle())) {
                        tmpList = tmpMap.get(pm.getTitle());
                        if (!tmpList.get(0).getOn_air_date().equals(pm.getOn_air_date())) {
                            tmpList = new ArrayList<>();
                        }
                    } else {
                        tmpList = new ArrayList<>();
                    }
                    tmpList.add(pm);
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
        for (Map.Entry<String, Map<String, List<PM>>> e : gatheredMap.entrySet()) {

            // 総合ブログの場合チーム名の取得とかが必要
            String tmp = "";
            for (Map.Entry<String, List<PM>> p : e.getValue().entrySet()) {
                PM pm = p.getValue().get(0);

                String teamName = "";

                String[] pTeamIdArr = pm.getTeamArr().split(",");
                if (pTeamIdArr.length > 0 && Long.parseLong(pTeamIdArr[0]) != (0L)) {
                    List<Long> pTeamIdList = Arrays.stream(pTeamIdArr).map(Long::parseLong).collect(Collectors.toList());
                    List<String> teamNameList = TeamEnum.findTeamNameListByTeamIdList(pTeamIdList);
                    teamName = String.join("/", teamNameList);
                }

                String memberName = "";

                String[] pMemIdArr = pm.getMemArr().split(",");
                if (pMemIdArr != null && pMemIdArr.length > 0 && Long.parseLong(pMemIdArr[0]) != (0L)) {
                    List<Long> pMemIdList = Arrays.stream(pMemIdArr).map(Long::parseLong).collect(Collectors.toList());
                    List<String> memberNameList = MemberEnum.findMNameListByIdList(pMemIdList);
                    memberName = String.join("/", memberNameList);
                }

                String description = pm.getDescription();
                tmp = tmp + "</br ><h6>" + dtf1.format(pm.getOn_air_date()) + ":　" + teamName + " " + memberName + "：" + pm.getTitle() + "</h6>" +
                        "<br /><p>番組概要：" + description + "</p>";

                String broad = "<p>放送局：";
                for (PM r : p.getValue()) {
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
                    broad = broad + stationNameList + "<br />";
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

    public String createDailyScheduleTitle(Date tmrw) {
        return sdf3.format(tmrw) + "の予定";
    }

    /**
     * その日の1日の予定画面のテキスト本文を作成します。
     *
     * @param teamId
     * @param tmrw
     * @param imMap
     * @param plist
     * @return content/ コンテンツ有無フラグ
     */
    public Map<String, Boolean> createDailySchedulePost(Long teamId, Date tmrw, Map<IM, List<ImVer>> imMap, List<PM> plist) {
        boolean tvContentFlg = true;

        String teamName = TeamEnum.get(teamId).getName();
        if (teamName.equals("NEWS ジャニーズ")) {
            teamName = "NEWS";
        }
        String date = "<h3>" + teamName + "</h3>";

        // TV
        List<String> pTextList = new ArrayList<>();
        for (PM pm : plist) {
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

            String tmp = dtf1.format(pm.getOn_air_date()) + "~ " + pm.getTitle() + stationNameList;
            pTextList.add(tmp);
        }

        if (pTextList.size() == 0) {
            tvContentFlg = false;
            pTextList.add("本日の出演情報はありません。確認次第更新します！");
        }

        // IM
        List<String> imTextList = new ArrayList<>();
        for (Map.Entry<IM, List<ImVer>> e : imMap.entrySet()) {
            String tmp = "<h4>" + e.getKey().getTitle() + "</h4>";
            String link = "";
            if (e.getValue().size() > 0) {
                List<String> tmpList = new ArrayList<>();
                for (ImVer ver : e.getValue()) {
                    String tmp1 = ver.getVer_name() + "\n[rakuten search='" + e.getKey().getTitle() + " " + ver.getVer_name() + "' kw='" + e.getKey().getTitle() + " " + ver.getVer_name() + "']";
                    tmpList.add(tmp1);
                }
                link = String.join("\n", sdf3.format(tmrw), String.join("\n", tmpList));
            } else {
                link = e.getKey().getTitle() + "\n[rakuten search='" + e.getKey().getTitle() + "' kw='" + e.getKey().getTitle() + "']";
            }
            imTextList.add(String.join("\n", tmp, link));
        }

        if (imTextList.size() == 0) {
            imTextList.add("本日発売予定は未確認です。確認次第更新します！");
        }

        boolean contentFlg = false;
        if (tvContentFlg) {
            contentFlg = true;
        }

        // SEO対策のためexternal/internal linkを用意する
        TeamEnum teamEnum = TeamEnum.get(teamId);
        String externalLink = "<h5>公式情報はこちらから</h5><br /><p><a href='" + teamEnum.getOfficialSite() + "' >公式サイト</a></p>";
        String internalLink = teamEnum.getInternalTop();
        if (internalLink == null) {
            internalLink = BlogEnum.get(teamEnum.getBlogEnumId()).getSubDomain();
        }
        return Collections.singletonMap(String.join("\n", date, "<h4>TV</h4>" , String.join("\n", pTextList), "<h4>発売</h4>", String.join("\n", imTextList), externalLink, internalLink), contentFlg);
    }

    /**
     * 単純に日付をstringにして返します。
     *
     * @param date
     * @return
     */
    public String dateToString(Date date) {
        return sdf3.format(date);
    }

    /**
     * 半角記号を全角に置き換えます
     *
     * @param originStr
     * @return
     */
    public String replaceSignals(String originStr) {
        String res = originStr;
        if (res.contains("(")) {
            res = res.replace("(", "（");
        }

        if (res.contains(")")) {
            res = res.replace(")", "）");
        }

        String tmpRes = "";
        if (res.contains("[") && res.contains("]")) {
            // 中括弧1つずつ削除すべきか判定して処理
            String[] resArr = res.split("]");
            for (String sArr : resArr) {
                if (!sArr.contains("[再")) {
                    tmpRes = tmpRes + sArr.replaceAll("\\[.*", "");
                } else {
                    tmpRes = tmpRes + sArr + "]";
                }
            }
        } else {
            tmpRes = res;
        }

        res = tmpRes;
        return res;
    }

    /**
     * wordpressのtagのslugを作成する
     *
     * @param source
     * @return
     */
    public String getTagSlug(String source) {
        // 大文字は小文字に
        String result = source.toLowerCase();
        // スペースは切り取る
        result = result.replaceAll(" ", "");
        result = result.replaceAll("　", "");
        // 記号は切り取る
        result = result.replaceAll("!", "");
        result = result.replaceAll("！", "");
        return result;
    }

    /**
     * amazon_imageから画像パスを引き抜き、"https:"をつけて返します
     *
     * @param amazonUrl
     * @return
     */
    public String shapeEyeCatchAmazonImage(String amazonUrl) {

        // 正規表現パターンを作成
        Pattern p = Pattern.compile("\\/\\/ws-fe.+?(?=\")");

        // マッチング
        Matcher m = p.matcher(amazonUrl);

        // 先頭からマッチングを繰り返す（ここでは一回しかマッチしない）
        if (m.find()) {
            String tmp = m.group(0);
            String finalPath = tmp.replace("_SL160", "_SL230");
            return "https:" + finalPath;
        }
        return "";
    }
}
