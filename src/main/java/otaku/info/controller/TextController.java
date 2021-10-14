package otaku.info.controller;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import otaku.info.dto.TeamIdMemberNameDto;
import otaku.info.dto.TwiDto;
import otaku.info.entity.*;
import otaku.info.enums.MagazineEnum;
import otaku.info.enums.MemberEnum;
import otaku.info.enums.PublisherEnum;
import otaku.info.enums.TeamEnum;
import otaku.info.searvice.*;
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

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    AnalyzeController analyzeController;

    @Autowired
    LineController lineController;

    @Autowired
    private ItemService itemService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StationService stationService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ItemMasterService itemMasterService;

    @Autowired
    private IMRelService IMRelService;

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
     * DateをStringにして返却します
     *
     * @param date
     * @return
     */
    public String dateToString(Date date) {
        return sdf3.format(date);
    }

    /**
     * Twitterポスト用のメッセージを作成します。
     *
     * @param twiDto
     * @return
     */
    public String twitter(TwiDto twiDto) {
        String tags = tagService.getTagByTeam(twiDto.getTeam_id()).stream().collect(Collectors.joining(" #","#",""));
        return "【PR】新商品の情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl() + "%0A%0A" + tags;
    }

    public String futureItemReminder(ItemMaster im, Long teamId, Item item) {
        int diff = dateUtils.dateDiff(new Date(), im.getPublication_date()) + 1;
        String tags = "";
        tags = tags + " " + tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
//        String blogUrl = blogDomainGenerator(teamId) + "item/" + im.getItem_m_id();
        String title = "";
        if (StringUtils.hasText(im.getTitle())) {
            title = im.getTitle();
        }

        return "【PR 発売まで" + diff + "日】%0A%0A" + title + "%0A発売日：" + sdf1.format(im.getPublication_date()) + "%0Aリンクはこちら↓%0A" + item.getUrl() + "%0A%0A" + tags;
    }

//    public String futureItemReminder(ItemMaster itemMaster, Item item, String teamIdStr) {
//        int diff = dateUtils.dateDiff(new Date(), item.getPublication_date()) + 1;
//        String tags = "";
//        tags = tags + " " + tagService.getTagByTeam(Long.parseLong(teamIdStr)).stream().collect(Collectors.joining(" #","#",""));
//        String blogUrl = setting.getBlogWebUrl() + "item/" + itemMaster.getItem_m_id();
//        String title = "";
//        if (StringUtils.hasText(itemMaster.getTitle())) {
//            title = itemMaster.getTitle();
//        }
//
//        return "【PR 発売まで" + diff + "日】%0A%0A" + title + "%0A発売日：" + sdf1.format(item.getPublication_date()) + "%0A詳細はブログへ↓%0A" + blogUrl + "%0A楽天購入はこちら↓%0A" + item.getUrl() + "%0A%0A" + tags;
//    }

    /**
     * 未来発売の商品のリマインダー文章を作成します。
     * Twitter用
     *
     * @param itemMaster
     * @param item
     * @param teamIdList
     * @return
     */
    public String futureItemReminder(ItemMaster itemMaster, Item item, List<Long> teamIdList) {
        int diff = dateUtils.dateDiff(new Date(), item.getPublication_date()) + 1;
        String tags = "";
        for (Long teamId : teamIdList) {
            tags = tags + " " + tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
        }
        String blogUrl = setting.getBlogWebUrl() + "item/" + itemMaster.getItem_m_id();
        String title = "";
        if (StringUtils.hasText(itemMaster.getTitle())) {
            title = itemMaster.getTitle();
        } else {
            title = item.getTitle().replaceAll("(\\[.*?\\])|(\\/)|(【.*?】)|(\\(.*?\\))|(\\（.*?\\）)", "");
            itemMaster.setTitle(title);
            // ついでに登録（更新）する
            itemMasterService.save(itemMaster);
        }
        return "【PR 発売まで" + diff + "日】%0A%0A" + title + "%0A発売日：" + sdf1.format(item.getPublication_date()) + "%0A詳細はブログへ↓%0A" + blogUrl + "%0A楽天購入はこちら↓%0A" + item.getUrl() + "%0A%0A" + tags;
    }

    /**
     * 未来発売の商品のリマインダー文章を作成します。
     *
     * @param itemMaster
     * @param item
     * @param teamId
     * @return
     */
    public String futureItemReminder(ItemMaster itemMaster, Item item, Long teamId) {
        int diff = dateUtils.dateDiff(new Date(), item.getPublication_date()) + 1;
        String tags = tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
//        String blogUrl = blogDomainGenerator(teamId) + "item/" + itemMaster.getItem_m_id();
        String title = "";
        if (StringUtils.hasText(itemMaster.getTitle())) {
            title = itemMaster.getTitle();
        } else {
            title = item.getTitle().replaceAll("(\\[.*?\\])|(\\/)|(【.*?】)|(\\(.*?\\))|(\\（.*?\\）)", "");
            itemMaster.setTitle(title);
            // ついでに登録（更新）する
            itemMasterService.save(itemMaster);
        }
        return "【PR 発売まで" + diff + "日】%0A%0A" + title + "%0A発売日：" + sdf1.format(item.getPublication_date()) + "%0Aリンクはこちら↓%0A" + item.getUrl() + "%0A楽天購入はこちら↓%0A" + item.getUrl() + "%0A%0A" + tags;
    }

    /**
     * 本日発売の商品のアナウンス文章を作る
     *
     * @param item
     * @return
     */
    public String releasedItemAnnounce(ItemMaster itemMaster, Item item) {
        String str1 = "【PR】本日発売！%0A%0A" + itemMaster.getTitle() + "%0A" + "詳細はこちら↓%0A"
                + setting.getBlogWebUrl() + "item/" + IMRelService.getWpIdByItemMId(itemMaster.getItem_m_id()) + "%0A" + "楽天リンクはこちら↓%0A"
                + item.getUrl();
        // TODO: twitterタグ、DB使わないで取れてる
        List<Long> teamIdList = IMRelService.findTeamIdListByItemMId(itemMaster.getItem_m_id());
        String tags = TeamEnum.findTeamNameListByTeamIdList(teamIdList).stream().collect(Collectors.joining(" #","#",""));
        return str1 + "%0A" + tags;
    }

    public String releasedItemAnnounce(ItemMaster itemMaster, Long teamId, Item item) {
//        String blogUrl = blogDomainGenerator(teamId) + "item/" + itemMaster.getItem_m_id();

        String str1 = "【PR】本日発売！%0A%0A" + itemMaster.getTitle() + "%0A" + "詳細はこちら↓%0A" + setting.getBlogWebUrl() + "item/" + IMRelService.getWpIdByItemMId(itemMaster.getItem_m_id()) + "%0A" + "詳細はこちら↓%0A" + item.getUrl();
        // TODO: twitterタグ、DB使わないで取れてる
        List<Long> teamIdList = IMRelService.findTeamIdListByItemMId(itemMaster.getItem_m_id());
        String tags = TeamEnum.findTeamNameListByTeamIdList(teamIdList).stream().collect(Collectors.joining(" #","#",""));
        return str1 + "%0A" + tags;
    }

    public String twitterPerson(TwiDto twiDto, String memberName) {
        String result = "【PR】" + memberName + "君の新商品情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl();
        if (result.length() + memberName.length() < 135) {
            result = "【PR】" + memberName + "君の新商品情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A#" + memberName + "%0A#" + twiDto.getUrl();
        }
        String tags = tagService.getTagByTeam(twiDto.getTeam_id()).stream().collect(Collectors.joining(" #","#",""));
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
        String blog = "一覧はこちら%0Ahttps://otakuinfo.fun/pages/" + TeamEnum.getTvPageId(teamId);
        return result + info + blog;
    }

    /**
     * 直近のTV番組のアラート文章を作ります。
     *
     * @param program
     * @return Map<ProgramId-TeamId, text>
     */
    public Map<String, String> tvAlert(Program program) {
        String stationName = stationService.getStationName(program.getStation_id());
        List<Long> teamIdList = pRelService.getTeamIdList(program.getProgram_id());

        Map<String, String> resultMap = new HashMap<>();
        // 返却するMapにKey(ProgramId)のみ詰め込みます。
        if (teamIdList.size() > 0) {
            // Mapのkeyを作り格納
            teamIdList.forEach(e -> resultMap.put(program.getProgram_id() + "-" + e, null));
        }

        // Member情報がある場合は情報を集める(TeamIdとMemberNameのDtoリスト)
        List<TeamIdMemberNameDto> teamIdMemberNameDtoList = new ArrayList<>();
        List<Long> memberIdList = pRelService.getMemberIdList(program.getProgram_id());
        for (Long mId : memberIdList) {
            TeamIdMemberNameDto dto = memberService.getMapTeamIdMemberName(mId);
            if (dto != null && dto.getMember_name() != null) {
                teamIdMemberNameDtoList.add(dto);
            }
        }

        // Member情報がある場合、DtoリストからMapへ詰め替えます。<TeamId, MemberIdList>
        Map<Long, String> keyMemberMap = new HashMap<>();
        if (teamIdMemberNameDtoList.size() > 0) {
            for (TeamIdMemberNameDto dto : teamIdMemberNameDtoList) {
                if (keyMemberMap.containsKey(dto.getTeam_id())) {
                    String v = keyMemberMap.get(dto.getTeam_id());
                    keyMemberMap.put(dto.getTeam_id(), v + "・" + dto.getMember_name());
                } else {
                    keyMemberMap.put(dto.getTeam_id(), dto.getMember_name());
                }
            }
        }

        // 返却リストにはKey(ProgramId)しか入っていないので、valueを入れます。
        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
            String num = entry.getKey();
            num = num.replaceAll("^.*-", "");
            Long teamId = Long.valueOf(num);
            String result = "";

            // Format LocalDateTime
            String formattedDateTime = program.getOn_air_date().format(dtf2);

            // Member情報のあるTeamの場合/ないTeamの場合で文章とタグが異なります。
            if (keyMemberMap.containsKey(teamId)) {
                result = "このあと" + formattedDateTime + "〜" + program.getTitle() + "(" + stationName + ")に、" + keyMemberMap.get(teamId) + "が出演します。ぜひご覧ください！";
                List<String> tagList = tagService.getTagByMemberNameList(Arrays.asList(keyMemberMap.get(teamId).split("・")));
                if (tagList.size() > 0) {
                    result = result + "%0A%0A" + tagList.stream().collect(Collectors.joining(" #","#",""));
                }
            } else {
                String teamName = teamService.getTeamName(teamId);
                if (!teamName.equals("")) {
                    result = "このあと" + formattedDateTime + "〜" + program.getTitle() + "(" + stationName + ")に、" +teamName  + "が出演します。ぜひご覧ください！";
                } else {
                    result = "このあと" + formattedDateTime + "〜" + program.getTitle() + "(" + stationName + ")に出演情報があります。ぜひご確認ください！";
                }

                List<String> tagList = tagService.getTagByTeam(teamId);
                if (tagList.size() > 0) {
                    result = result + "%0A%0A" + tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
                }
            }
            resultMap.put(entry.getKey(), result);
        }
        return resultMap;
    }

    /**
     * WordPressブログのリリース情報固定ページ表示用のテキストを作成
     *
     * @param todayMap
     * @param futureMap
     * @return
     */
    public String blogUpdateReleaseItems(Map<ItemMaster, List<Item>> todayMap, Map<ItemMaster, List<Item>> futureMap) {
        String result = "[toc depth='4']";

        // 今日の/先1週間の商品ごとの文章を作る(List<商品のテキスト>)
        List<String> todaysElems = todayMap == null ? new ArrayList<>() : blogReleaseItemsText(todayMap);
        List<String> futureElems = futureMap == null ? new ArrayList<>() : blogReleaseItemsText(futureMap);

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
     *
     * @param itemMasterListMap
     * @return
     */
    public List<String> blogReleaseItemsText(Map<ItemMaster, List<Item>> itemMasterListMap) {
        List<String> resultList = new ArrayList<>();

        // マスター商品ごとにテキストを作り返却リストに入れる(Itemリストのサイズが0以上のマスタ商品をタイトルでソート)。
        for (Map.Entry<ItemMaster, List<Item>> entry : itemMasterListMap.entrySet().stream().filter(e -> e.getValue().size() > 0).sorted(Comparator.comparing(e -> e.getKey().getTitle())).collect(Collectors.toList())) {
            ItemMaster itemMaster = entry.getKey();
            List<Item> itemList = entry.getValue().stream().filter(e -> StringUtils.hasText(e.getItem_code())).collect(Collectors.toList());
            boolean noRakutenFlg = itemList.size() == 0;

            String date = dateUtils.getDay(itemMaster.getPublication_date());
            String publicationDate = sdf1.format(itemMaster.getPublication_date()) + "(" + date + ")";

            // チーム名が空だった場合正確性に欠けるため、続きの処理には進まず次の商品に進む
            if (IMRelService.findTeamIdListByItemMId(itemMaster.getItem_m_id()) == null) {
                continue;
            }

            List<String> teamNameList = teamService.findTeamNameByIdList(IMRelService.findTeamIdListByItemMId(itemMaster.getItem_m_id()));
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
            h2 = "<h2 id=id_" + itemMaster.getItem_m_id() + ">" + h2 + "</h2>";

            Integer estPrice = noRakutenFlg ? getPrice(entry.getValue()) : getPrice(itemList);

            String headItem = "[rakuten search=" + itemMaster.getTitle() + " kw=" + itemMaster.getTitle() + " amazon=1 rakuten=1 yahoo=1]";

            String description = "<h6>概要</h6>" + "<p>" + itemMaster.getItem_caption() + "</p>";

            String price = "<h6>価格</h6>" + "<p>" + estPrice + "円</p>";

            String pubDate = sdf1.format(itemMaster.getPublication_date());
            String publicationDateStr = "<h6>発売日</h6>" + "<p>" + pubDate + "</p>";

            String text = String.join("\n", h2, headItem, description, price, publicationDateStr);
            // 返却リストに追加する
            resultList.add(text);
        }
        return resultList;
    }

    /**
     * 商品の金額（多分これが正しい）を返す
     *
     * @param itemList
     * @return
     */
    private Integer getPrice(List<Item> itemList) {
        List<Integer> priceList = itemList.stream().map(e -> e.getPrice()).distinct().collect(Collectors.toList());
        if (priceList.size() == 1) {
            return priceList.get(0);
        } else {
            return priceList.stream().max(Integer::compare).orElse(0);
        }
    }

    /**
     * チームIDリストからチーム名リストを返します。
     *
     * @param teamIdList
     * @return
     */
    private List<String> findTeamName(List<Long> teamIdList) {
        if (teamIdList == null || teamIdList.size() == 0) {
            return new ArrayList<>();
        }
        return teamService.findTeamNameByIdList(teamIdList);
    }

    /**
     * メンバーIDリストからメンバー名リストを返します。
     *
     * @param memberIdList
     * @return
     */
    private List<String> findMemberName(List<Long> memberIdList) {
        if (memberIdList == null || memberIdList.size() == 0) {
            return new ArrayList<>();
        }
        return memberService.getMemberNameList(memberIdList);
    }

    /**
     * レーベンシュタイン距離で文字列の類似度を判定
     * @param s1
     * @param s2
     * https://qiita.com/hakozaki/items/856230d3f8e29d3302d6
     * @return
     */
    public int getSimilarScoreByLevenshteinDistance(String s1, String s2){

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
    public int getSimilarScoreByJaroWinklerDistance(String s1, String s2){

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
            return "";
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
                lineController.post("ItemMasterの登録で雑誌名が見つかりませんでした。itemId=" + item.getItem_id());
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
        System.out.println("textController:568 publicationDate=" + publicationDate);
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

    /**
     * 引数で受けたサブドメインからリクエストに使用するドメインを作成します。
     * 引数のサブドメインがnullの場合は、総合ブログ（親）のパスを返します。
     *
     * @param teamId
     * @return
     */
    private String blogDomainGenerator(Long teamId) {
        TeamEnum e = TeamEnum.get(teamId);
        String url = "";

        if (e != null) {
            String subDomain = e.getSubDomain();
            // 総合ブログのsubdomain"NA"に合致しない場合とする場合で分けてる
            if (subDomain != null && !subDomain.equals("")) {
                url = setting.getBlogHttps() + subDomain + setting.getBlogDomain();
            }
        }

        if (url.equals("")) {
            url = setting.getBlogWebUrl();
        }
        return url;
    }
}
