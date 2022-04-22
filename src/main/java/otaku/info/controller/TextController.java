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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private ImVerService imVerService;

    @Autowired
    private IMRelMemService imRelMemService;

    @Autowired
    private PRelService pRelService;

    @Autowired
    private Setting setting;

    private final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("M/d");
    private final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd");
    private final DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("hh:mm");
    private final DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("MM/dd HH:mm");
    private final DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyyMMdd");

   /**
     * Twitterポスト用のメッセージを作成します。
     *
     * @param twiDto
     * @return
     */
    public String twitter(TwiDto twiDto) {
        String tags = "#" + TeamEnum.get(twiDto.getTeam_id()).getMnemonic();
        return "【PR】新商品の情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl() + "%0A" + tags;
    }

    /**
     * 本日発売の商品のアナウンス文章を作る
     *
     * @param item
     * @return
     */
//    public String releasedItemAnnounce(ItemMaster itemMaster, Item item) {
//        String url = IMRelService.findByImIdTeamId(itemMaster.getItem_m_id())
//        String str1 = "【PR】本日発売！%0A%0A" + itemMaster.getTitle() + "%0A" + "詳細はこちら↓%0A"
//                + akansetting.getBlogWebUrl() + "item/" + IMRelService.getWpIdByItemMId(itemMaster.getItem_m_id()) + "%0A" + "楽天リンクはこちら↓%0A"
//                + item.getUrl();
//        // TODO: twitterタグ、DB使わないで取れてる
//        List<Long> teamIdList = IMRelService.findTeamIdListByItemMId(itemMaster.getItem_m_id());
//        String tags = TeamEnum.findTeamNameListByTeamIdList(teamIdList).stream().collect(Collectors.joining(" #","#",""));
//        return str1 + "%0A" + tags;
//    }
//
//    public String releasedItemAnnounce(ItemMaster itemMaster, Long teamId, Item item) {
////        String blogUrl = blogDomainGenerator(teamId) + "item/" + itemMaster.getItem_m_id();
//
//        String str1 = "【PR】本日発売！%0A%0A" + itemMaster.getTitle() + "%0A" + "詳細はこちら↓%0A" + setting.getBlogWebUrl() + "item/" + IMRelService.getWpIdByItemMId(itemMaster.getItem_m_id()) + "%0A" + "詳細はこちら↓%0A" + item.getUrl();
//        // TODO: twitterタグ、DB使わないで取れてる
//        List<Long> teamIdList = IMRelService.findTeamIdListByItemMId(itemMaster.getItem_m_id());
//        String tags = TeamEnum.findTeamNameListByTeamIdList(teamIdList).stream().collect(Collectors.joining(" #","#",""));
//        return str1 + "%0A" + tags;
//    }

    /**
     * TV出演情報があるグループのTwitter投稿文
     *
     * @param ele Mapから抜き取ったEntry(key=TeamId,value=List<Program>)
     * @param forToday 今日の情報(TRUE)、明日の情報(FALSE)
     * @param date 情報の日付
     * @return
     */
    public String tvPost(Map.Entry<Long, List<Program>> ele, boolean forToday, Date date, Long teamId) {
        String dateStr = forToday ? "今日(" + sdf2.format(date) + ")" : "明日(" + sdf2.format(date) + ")";
        String teamName = teamService.getTeamName(ele.getKey());
        String result= "";
        if (!teamName.equals("")) {
            result = dateStr + "の" + teamName + "のTV出演情報です。%0A%0A";
        } else {
            result = dateStr + "のTV出演情報です。%0A%0A";
        }

        String info = "";
        for (Program p : ele.getValue()) {
            info = info + dtf1.format(p.getOn_air_date()) + " " + p.getTitle() + " (" + stationService.getStationName(p.getStation_id()) + ")%0A";
        }

        // blogへの誘導
        String blog = "一覧はこちら%0A" + TeamEnum.findSubDomainById(teamId) + "pages/" + TeamEnum.getTvPageId(teamId);
        return result + info + blog;
    }

    /**
     * 直近のTV番組のアラート文章を作ります。
     *
     * @param program
     * @return Map<ProgramId-TeamId, text>
     */
//    public Map<String, String> tvAlert(Program program) {
//        String stationName = stationService.getStationName(program.getStation_id());
//        List<Long> teamIdList = pRelService.getTeamIdList(program.getProgram_id());
//
//        Map<String, String> resultMap = new HashMap<>();
//        // 返却するMapにKey(ProgramId)のみ詰め込みます。
//        if (teamIdList.size() > 0) {
//            // Mapのkeyを作り格納
//            teamIdList.forEach(e -> resultMap.put(program.getProgram_id() + "-" + e, null));
//        }
//
//        // Member情報がある場合は情報を集める(TeamIdとMemberNameのDtoリスト)
//        List<TeamIdMemberNameDto> teamIdMemberNameDtoList = new ArrayList<>();
//        List<Long> memberIdList = pRelService.getMemberIdList(program.getProgram_id());
//        for (Long mId : memberIdList) {
//            TeamIdMemberNameDto dto = memberService.getMapTeamIdMemberName(mId);
//            if (dto != null && dto.getMember_name() != null) {
//                teamIdMemberNameDtoList.add(dto);
//            }
//        }
//
//        // Member情報がある場合、DtoリストからMapへ詰め替えます。<TeamId, MemberIdList>
//        Map<Long, String> keyMemberMap = new HashMap<>();
//        if (teamIdMemberNameDtoList.size() > 0) {
//            for (TeamIdMemberNameDto dto : teamIdMemberNameDtoList) {
//                if (keyMemberMap.containsKey(dto.getTeam_id())) {
//                    String v = keyMemberMap.get(dto.getTeam_id());
//                    keyMemberMap.put(dto.getTeam_id(), v + "・" + dto.getMember_name());
//                } else {
//                    keyMemberMap.put(dto.getTeam_id(), dto.getMember_name());
//                }
//            }
//        }
//
//        // 返却リストにはKey(ProgramId)しか入っていないので、valueを入れます。
//        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
//            String num = entry.getKey();
//            num = num.replaceAll("^.*-", "");
//            Long teamId = Long.valueOf(num);
//            String result = "";
//
//            // Format LocalDateTime
//            String formattedDateTime = program.getOn_air_date().format(dtf2);
//
//            // Member情報のあるTeamの場合/ないTeamの場合で文章とタグが異なります。
//            if (keyMemberMap.containsKey(teamId)) {
//                result = "このあと" + formattedDateTime + "〜" + program.getTitle() + "(" + stationName + ")に、" + keyMemberMap.get(teamId) + "が出演します。ぜひご覧ください！";
//                List<String> tagList = tagService.getTagByMemberNameList(Arrays.asList(keyMemberMap.get(teamId).split("・")));
//                if (tagList.size() > 0) {
//                    result = result + "%0A%0A" + tagList.stream().collect(Collectors.joining(" #","#",""));
//                }
//            } else {
//                String teamName = teamService.getTeamName(teamId);
//                if (!teamName.equals("")) {
//                    result = "このあと" + formattedDateTime + "〜" + program.getTitle() + "(" + stationName + ")に、" +teamName  + "が出演します。ぜひご覧ください！";
//                } else {
//                    result = "このあと" + formattedDateTime + "〜" + program.getTitle() + "(" + stationName + ")に出演情報があります。ぜひご確認ください！";
//                }
//
//                List<String> tagList = tagService.getTagByTeam(teamId);
//                if (tagList.size() > 0) {
//                    result = result + "%0A%0A" + tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
//                }
//            }
//            resultMap.put(entry.getKey(), result);
//        }
//        return resultMap;
//    }

    /**
     * WordPressブログのリリース情報固定ページ表示用のテキストを作成
     *
     * @param todayMap
     * @param futureMap
     * @return
     */
    public String blogUpdateReleaseItems(Map<IM, List<Item>> todayMap, Map<IM, List<Item>> futureMap, String subDomain) {

        String imagePath = TeamEnum.getBySubDomain(subDomain).getScheduleImagePath();

        String result = "[toc depth='4']";

        // 今日の/先1週間の商品ごとの文章を作る(List<商品のテキスト>)
        List<String> todaysElems = todayMap == null ? new ArrayList<>() : blogReleaseItemsText(todayMap, imagePath);
        List<String> futureElems = futureMap == null ? new ArrayList<>() : blogReleaseItemsText(futureMap, imagePath);

        // 本日発売の商品
        if (todaysElems.size() > 0) {
            result = result + "\n" + String.join("\n\n", String.join("\n\n", todaysElems));
        } else {
            result = result + "\n" + "<h2>今日発売の商品はありません。</h2>";
        }

        // 明日以降発売の商品
        String result2 = "";
        if (futureElems.size() > 0) {
            result2 = String.join("\n\n", result, String.join("\n\n", futureElems));
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
     * @param itemMasterListMap
     * @param imagePath
     * @return
     */
    public List<String> blogReleaseItemsText(Map<IM, List<Item>> itemMasterListMap, String imagePath) {
        List<String> resultList = new ArrayList<>();

        logger.debug("blogReleaseItemsTextの中です");
        logger.debug("itemMasterListMap.size=" + itemMasterListMap.size());
        // マスター商品ごとにテキストを作り返却リストに入れる(Itemリストのサイズが0以上のマスタ商品をタイトルでソート)。
        for (Map.Entry<IM, List<Item>> entry : itemMasterListMap.entrySet()) {
            IM itemMaster = entry.getKey();

            String date = dateUtils.getDay(itemMaster.getPublication_date());
            String publicationDate = sdf1.format(itemMaster.getPublication_date()) + "(" + date + ")";

            List<ImVer> verList = imVerService.findByImId(itemMaster.getIm_id());

            // チーム名が空だった場合正確性に欠けるため、続きの処理には進まず次の商品に進む
            if (iMRelService.findTeamIdListByItemMId(itemMaster.getIm_id()) == null) {
                continue;
            }

            List<String> teamNameList = teamService.findTeamNameByIdList(iMRelService.findTeamIdListByItemMId(itemMaster.getIm_id()));
            String teamNameUnited = String.join(" ", teamNameList);

            // h2で表示したい商品のタイトルを生成
            String h2 = "";
            // TODO: メンバー名今なし
            // メンバー名もある場合はこちら
//            List<Long> memberIdList = imRelMemService.findMemberIdListByRelId();
//                    IMRelService.findMemberIdListByItemMId(itemMaster.getItem_m_id());
//            if (memberIdList.size() > 0) {
//                List<String> memberNameList = memberService.getMemberNameList(memberIdList);
//                h2 = String.join(" ", publicationDate, teamNameUnited, String.join(" ", memberNameList), itemMaster.getTitle());
//            } else {
                // メンバー名ない場合はこちら
                h2 = String.join(" ", publicationDate, teamNameUnited, itemMaster.getTitle());
//            }

            // htmlタグ付与
            h2 = "<h2 id=id_" + itemMaster.getIm_id() + ">" + h2 + "</h2>";

            // SEO対策のため画像を入れる
            String seoImage = "";
            if (!imagePath.isBlank()) {
                seoImage = "<figure class='wp-block-image size-large'><img src=" + imagePath + "' alt='' class='wp-image-6736'/></figure>";
            }

            List<String> verTxtList = new ArrayList<>();
            String txt = "";
            if (verList.size() > 0) {
                for (ImVer ver : verList) {
                    txt = ver.getVer_name() + "\n" + "[rakuten search='" + itemMaster.getTitle() + " " + ver.getVer_name() + "' kw='" + itemMaster.getTitle() + " " + ver.getVer_name() + "' amazon=1 rakuten=1 yahoo=1]";
                    verTxtList.add(txt);
                }
            } else {
                txt = itemMaster.getTitle() + "\n" + "[rakuten search='" + itemMaster.getTitle() + "' kw='" + itemMaster.getTitle() + "' amazon=1 rakuten=1 yahoo=1]";
            }

            String pubDate = sdf1.format(itemMaster.getPublication_date());
            String publicationDateStr = "<h6>発売日</h6>" + "<p>" + pubDate + "</p>";

            // SEO対策：external linkとしてグループの公式サイトのリンク
            String externalLink = "";
            for (String teamName : teamNameList) {
                String tmpLink = TeamEnum.get(teamName).getOfficialSite();
                tmpLink = "<a href='" + tmpLink + "'><p>" + teamName + "公式サイト</p></a>";
                if (externalLink.isBlank()) {
                    externalLink = tmpLink;
                } else {
                    String tmp = externalLink;
                    externalLink = tmp + "<br />" + tmpLink;
                }
            }

            // SEO対策：internal linkとしてグループの公式サイトのリンク
            String internalLink = "";
            for (String teamName : teamNameList) {
                TeamEnum teamEnum = TeamEnum.get(teamName);
                String tmpLink = teamEnum.getInternalTop();

                if (tmpLink == null) {
                    tmpLink = "<a href='" + teamEnum.getSubDomain() + "'><p>" + teamName + "トップ</p></a>";
                } else {
                    tmpLink = "<a href='" + tmpLink + "'><p>" + teamName + "トップ</p></a>";
                }

                if (internalLink.isBlank()) {
                    internalLink = tmpLink;
                } else {
                    String tmp = internalLink;
                    internalLink = tmp + "<br />" + tmpLink;
                }
            }

            String text = String.join("\n", h2, publicationDateStr, seoImage, itemMaster.getAmazon_image(), String.join("\n", verTxtList), externalLink, internalLink);
            // 返却リストに追加する
            resultList.add(text);
        }
        logger.debug("resultList.size=" + resultList.size());
        return resultList;
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
     * itemMasterからの発売日とタイトルを引数に想定
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
     * ItemMasterのタイトルを作成します。
     *
     * @return
     */
    public String createItemMasterTitle(List<Item> itemList, Date publicationDate) {
        // Id, count(カウントが多い方が信頼性がある)
        Map<Long, Integer> teamIdMap = new HashMap<>();
        Map<Long, Integer>  memberIdMap = new HashMap<>();
        Map<Long, Integer>  magazineEnumsIdMap = new HashMap<>();
        Map<Long, Integer>  publisherEnumIdMap = new HashMap<>();

        for (Item item : itemList) {
            if (!StringUtils.hasText(item.getTitle())) {
                continue;
            }

            // それぞれのItemについて、チーム名、メンバー名、出版社名、雑誌名がないか調べ、あったらリストに追加していきたい
            // team名
            List<Long> tmpList = teamService.findTeamIdListByText(item.getTitle());
            if (tmpList != null && !tmpList.isEmpty()) {
                for (Long id : tmpList) {
                    Integer count = 0;
                    if (teamIdMap.containsKey(id)) {
                        count = teamIdMap.get(id);
                    }
                    teamIdMap.put(id, ++count);
                }
            }

            // メンバー名を追加する
            tmpList = memberService.findMemberIdByText(item.getTitle());
            if (tmpList != null && !tmpList.isEmpty()) {
                for (Long id : tmpList) {
                    Integer count = 0;
                    if (memberIdMap.containsKey(id)) {
                        count = memberIdMap.get(id);
                    }
                    memberIdMap.put(id, ++count);
                }
            }

            // 雑誌名を追加する。雑誌名が見つかった場合、出版社も探す
            tmpList = MagazineEnum.findMagazineIdByText(item.getTitle());
            if (tmpList != null && !tmpList.isEmpty()) {
                for (Long id : tmpList) {
                    Integer count = 0;
                    if (magazineEnumsIdMap.containsKey(id)) {
                        count = magazineEnumsIdMap.get(id);
                    }
                    magazineEnumsIdMap.put(id, ++count);
                }

                // 出版社名を追加する
                tmpList = PublisherEnum.findPublisherIdByText(item.getTitle());
                if (tmpList.size() > 0) {
                    for (Long id : tmpList) {
                        Integer count = 0;
                        if (publisherEnumIdMap.containsKey(id)) {
                            count = publisherEnumIdMap.get(id);
                        }
                        publisherEnumIdMap.put(id, ++count);
                    }
                }
            } else {
                // 雑誌名が見つからなかった場合、その旨をLINE通知する
                logger.debug("ItemMasterの登録で雑誌名が見つかりませんでした。itemId=" + item.getItem_id());
            }

        }

        // 採点値より、確実性の低いデータは捨てる
        List<Long> teamIdList = new ArrayList<>();
        List<Long> memberIdList = new ArrayList<>();
        List<Long> magazineIdList = new ArrayList<>();
        List<Long> publisherIdList = new ArrayList<>();

        if (teamIdMap.size() > 0) {
            teamIdList = teamIdMap.entrySet().stream().filter(e -> (float) e.getValue()/ itemList.size() >= 0.5).sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).map(Map.Entry::getKey).collect(Collectors.toList());
        }

        if (memberIdMap.size() > 0) {
            memberIdList = memberIdMap.entrySet().stream().filter(e -> (float) e.getValue()/itemList.size() >= 0.5).sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).map(Map.Entry::getKey).collect(Collectors.toList());
        }

        // 雑誌名が見つかってる場合、出版社情報も入れる
        if (magazineEnumsIdMap.size() > 0) {
            magazineIdList = magazineEnumsIdMap.entrySet().stream().filter(e -> (float) e.getValue()/itemList.size() >= 0.5).sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).map(Map.Entry::getKey).collect(Collectors.toList());

            if (publisherEnumIdMap.size() > 0) {
                publisherIdList = publisherEnumIdMap.entrySet().stream().filter(e -> (float) e.getValue()/itemList.size() >= 0.5).sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).map(Map.Entry::getKey).collect(Collectors.toList());
            }
        }

        // 追加処理。メンバー見つかってるのにチームが対応してなかったら入れてあげる
        if (memberIdList.size() > 0) {
            // Serviceからの返却値は重複も含むので、distinctで抜いてあげる
            List<Long> teamIdOfMemberList = memberService.findTeamIdListByMemberIdList(memberIdList).stream().distinct().collect(Collectors.toList());
            if (teamIdList.size() != teamIdOfMemberList.size()) {
                if (teamIdList.size() > teamIdOfMemberList.size()) {
                    // teamIdOfMemberListの要素が全部入ってるかの確認をする。入ってなかったら追加
                    List<Long> tmpList = new ArrayList<>();
                    for (Long id : teamIdList) {
                        if (!teamIdOfMemberList.contains(id)) {
                            tmpList.add(id);
                        }
                    }
                    if (tmpList.size() > 0) {
                        teamIdList.addAll(tmpList);
                    }
                } else {
                    // teamIdOfMemberListの方がサイズが大きい場合、既存のteamIdList要素を全て削除したdiffリストを用意、残った要素があったらteamIdListに追加する
                    List<Long> diff = new ArrayList<>(teamIdOfMemberList);
                    diff.removeAll(teamIdList);
                    teamIdList.addAll(diff);
                }
            }
        }
        // 全てのItemからデータを抜いたので、これから返却するタイトルを作成する
        String res = "";
        if (publicationDate != null) {
            res = sdf1.format(publicationDate);
        }

        if (teamIdList.size() > 0) {
            List<String> teamNameList = teamService.findTeamNameByIdList(teamIdList);
            res = res + " " + teamNameList.stream().collect(Collectors.joining(" ", "", ""));
        }

        if (memberIdList.size() > 0) {
            List<String> memberNameList = memberService.getMemberNameList(memberIdList);
            res = res + " " +  memberNameList.stream().collect(Collectors.joining(" ", "", ""));
        }

        // 雑誌名が見つかってる場合、出版社情報も入れる
        if (magazineIdList.size() > 0) {
            List<String> magazineNameList = MagazineEnum.findMagazineNameList(magazineIdList);
            res = res + " " + magazineNameList.stream().collect(Collectors.joining(" ", "", ""));

            if (publisherIdList.size() > 0) {
                List<String> publisherNameList = PublisherEnum.findPublisherNameList(publisherIdList);
                res = res + " " + publisherNameList.stream().collect(Collectors.joining(" ", "", ""));
            }
        } else {
            // 雑誌じゃない場合||Enumに雑誌名が見つからなかった場合の処理
            res = res + " " + itemList.get(0).getTitle();
        }

        return res;
    }

    /**
     * TV番組固定ページのテキストを作成。
     * 1つのドメインにポストするProgramリストが日にちごちゃ混ぜで入ってくる
     *
     * @param programList
     * @return
     */
    public String tvPageText(List<Program> programList, String subDomain) throws ParseException {
        if (programList.size() == 0) {
            return "";
        }

        String result = "[toc depth='6']";
        // 丁寧にプログラムをソートする（放送日、チーム）
        // 1:日付ごとにまとめる<DateStr, List<Program>>
        Map<String, List<Program>> datePMap = new TreeMap<>();
        for (Program p : programList) {

            String targetDate = dtf3.format(p.getOn_air_date());
            List<Program> tmpList;
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
        Map<String, Map<String, List<Program>>> gatheredMap = new TreeMap<>();
        if (datePMap.size() > 0) {
            for (Map.Entry<String, List<Program>> e : datePMap.entrySet()) {
                Map<String, List<Program>> tmpMap = new TreeMap<>();
                List<Program> tmpList;
                for (Program p : e.getValue()) {
                    if (tmpMap.containsKey(p.getTitle())) {
                        tmpList = tmpMap.get(p.getTitle());
                        if (!tmpList.get(0).getOn_air_date().equals(p.getOn_air_date())) {
                            tmpList = new ArrayList<>();
                        }
                    } else {
                        tmpList = new ArrayList<>();
                    }
                    tmpList.add(p);
                    tmpMap.put(p.getTitle(), tmpList);
                }
                gatheredMap.put(e.getKey(), tmpMap);
            }
        }

        // ソートされた日付ごとのマップができたので、それぞれの日の文章を作成する
        // 結果をまとめるリスト<h2, 番組ごとのテキストのリスト>
        Map<String, List<String>> textByDays = new TreeMap<>();
        List<String> textList = new ArrayList<>();
        String h2 = "";
        for (Map.Entry<String, Map<String, List<Program>>> e : gatheredMap.entrySet()) {

            // 総合ブログの場合チーム名の取得とかが必要
            String tmp = "";
            for (Map.Entry<String, List<Program>> p : e.getValue().entrySet()) {
                Program masterP = p.getValue().get(0);

                String teamName = "";
                if (subDomain.equals("NA")) {
                    List<Long> pTeamIdList = pRelService.getTeamIdList(masterP.getProgram_id());
                    if (pTeamIdList != null && !pTeamIdList.isEmpty() && !pTeamIdList.get(0).equals(0L)) {
                        List<String> teamNameList = TeamEnum.findTeamNameListByTeamIdList(pTeamIdList);
                        teamName = String.join("/", teamNameList);
                    }
                }

                String memberName = "";
                List<Long> memberIdList = pRelService.getMemberIdList(masterP.getProgram_id());
                if (memberIdList != null && !memberIdList.isEmpty() && memberIdList.get(0) != null && !memberIdList.get(0).equals(0L)) {

                    List<String> memberNameList = MemberEnum.findMNameListByIdList(memberIdList);
                    memberName = String.join("/", memberNameList);
                }

                String description = StringUtils.hasText(masterP.getDescription()) ? masterP.getDescription() : "";
                tmp = tmp + "</br ><h6>" + dtf1.format(masterP.getOn_air_date()) + ":　" + teamName + " " + memberName + "：" + masterP.getTitle() + "</h6><br /><p>番組概要：" + description + "</p>";

                String broad = "<p>放送局：";
                for (Program r : p.getValue()) {
                    String stationName = stationService.getStationName(r.getStation_id());
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
    public Map<String, Boolean> createDailySchedulePost(Long teamId, Date tmrw, Map<IM, List<ImVer>> imMap, List<Program> plist) {
        boolean tvContentFlg = true;
        boolean imContentFlg = true;

        String date = "<h3>" + TeamEnum.get(teamId).getName() + "</h3>";

        // TV
        List<String> pTextList = new ArrayList<>();
        for (Program p : plist) {
            StationEnum e = StationEnum.get(p.getStation_id());
            String stationName = "";
            if (e.equals(StationEnum.NHK)) {
                Station s = stationService.findById(p.getStation_id());
                if (s != null) {
                    stationName = s.getStation_name();
                }
            } else {
                stationName = e.getName();
            }
            String tmp = dtf1.format(p.getOn_air_date()) + "~ " + p.getTitle() + "(" + stationName + ")";
            pTextList.add(tmp);
        }

        if (pTextList.size() == 0) {
            tvContentFlg = false;
            pTextList.add("本日の出演情報はありません。確認次第追記します！");
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
            imContentFlg = false;
            imTextList.add("本日発売予定は未確認です。確認次第追記します！");
        }

        boolean contentFlg = false;
        if (tvContentFlg || tvContentFlg) {
            contentFlg = true;
        }

        // SEO対策のためexternal/internal linkを用意する
        TeamEnum teamEnum = TeamEnum.get(teamId);
        String externalLink = teamEnum.getOfficialSite();
        String internalLink = teamEnum.getInternalTop();
        if (internalLink == null) {
            internalLink = teamEnum.getSubDomain();
        }
        return Collections.singletonMap(String.join("\n", date, "<h2>TV</h2>" , String.join("\n", pTextList), "<h2>発売</h2>", String.join("\n", imTextList), externalLink, internalLink), contentFlg);
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
        if (originStr.contains("(")) {
            originStr.replace("(", "（");
        }

        if (originStr.contains(")")) {
            originStr.replace(")", "）");
        }

        return originStr;
    }
}
