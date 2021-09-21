package otaku.info.controller;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import otaku.info.dto.TeamIdMemberNameDto;
import otaku.info.dto.TwiDto;
import otaku.info.entity.*;
import otaku.info.searvice.*;
import otaku.info.setting.Setting;
import otaku.info.utils.DateUtils;

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
    private Setting setting;

    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("M/d");
    private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd");
    private DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("hh:mm");
    private DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("MM/dd HH:mm");

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

    public String futureItemReminder(ItemMaster itemMaster, Item item, String teamIdStr) {
        int diff = dateUtils.dateDiff(new Date(), item.getPublication_date()) + 1;
        String tags = "";
        tags = tags + " " + tagService.getTagByTeam(Long.parseLong(teamIdStr)).stream().collect(Collectors.joining(" #","#",""));
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

    public String futureItemReminder(ItemMaster itemMaster, Item item, Long teamId) {
        int diff = dateUtils.dateDiff(new Date(), item.getPublication_date()) + 1;
        String tags = tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
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
     * 本日発売の商品のアナウンス文章を作る
     *
     * @param item
     * @return
     */
    public String releasedItemAnnounce(ItemMaster itemMaster, Item item) {
        String str1 = "【PR】本日発売！%0A%0A" + itemMaster.getTitle() + "%0A" + "詳細はこちら↓%0A"
                + setting.getBlogWebUrl() + "item/" + itemMaster.getWp_id() + "%0A" + "楽天リンクはこちら↓%0A"
                + item.getUrl();
        String tags = tagService.getTagByTeam(Long.parseLong(itemMaster.getTeam_id())).stream().collect(Collectors.joining(" #","#",""));
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
    public String tvPost(Map.Entry<Long, List<Program>> ele, boolean forToday, Date date) {
        String dateStr = forToday ? "今日(" + sdf2.format(date) + ")" : "明日(" + sdf2.format(date) + ")";
        String result = dateStr + "の" + teamService.getTeamName(ele.getKey()) + "のTV出演情報です。%0A%0A";

        String info = null;
        for (Program p : ele.getValue()) {
            info = info + dtf1.format(p.getOn_air_date()) + " " + p.getTitle() + " (" + stationService.getStationName(p.getStation_id()) + ")%0A";
        }

        // blogへの誘導
        String blog = "一覧はこちら%0Ahttps://otakuinfo.fun/pages/1707";
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
        List<Long> teamIdList = new ArrayList<>();

        // TeamIdが空でなければリストに追加します。
        if (program.getTeam_id() != null && !program.getTeam_id().equals("")) {
            if (program.getTeam_id().contains(",")) {
                teamIdList = List.of(program.getTeam_id().split(","))
                        .stream().map(Integer::parseInt).collect(Collectors.toList())
                        .stream().map(Integer::longValue).collect(Collectors.toList());
            } else {
                teamIdList.add((long)Integer.parseInt(program.getTeam_id()));
            }
        }

        Map<String, String> resultMap = new HashMap<>();
        // 返却するMapにKey(ProgramId)のみ詰め込みます。
        if (teamIdList.size() > 0) {
            // Mapのkeyを作り格納
            teamIdList.forEach(e -> resultMap.put(program.getProgram_id() + "-" + e, null));
        }

        // Member情報がある場合は情報を集める(TeamIdとMemberNameのDtoリスト)
        List<TeamIdMemberNameDto> teamIdMemberNameDtoList = new ArrayList<>();
        if (program.getMember_id() != null && !program.getMember_id().equals("")) {
            // Member情報格納
            if (program.getMember_id().contains(",")) {
                List.of(program.getMember_id().split(","))
                        .stream().map(Integer::parseInt).collect(Collectors.toList())
                        .stream().map(Integer::longValue).collect(Collectors.toList())
                        .forEach(e -> teamIdMemberNameDtoList.add(memberService.getMapTeamIdMemberName(e)));
            } else {
                teamIdMemberNameDtoList.add(memberService.getMapTeamIdMemberName((long) Integer.parseInt(program.getMember_id())));
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
            String tags = "";
            // Format LocalDateTime
            String formattedDateTime = program.getOn_air_date().format(dtf2);

            // Member情報のあるTeamの場合/ないTeamの場合で文章とタグが異なります。
            if (keyMemberMap.containsKey(teamId)) {
                result = "このあと" + formattedDateTime + "〜" + program.getTitle() + "(" + stationName + ")に、" + keyMemberMap.get(teamId) + "が出演します。ぜひご覧ください！";
                tags = tagService.getTagByMemberNameList(Arrays.asList(keyMemberMap.get(teamId).split("・"))).stream().collect(Collectors.joining(" #","#",""));
            } else {
                result = "このあと" + formattedDateTime + "〜" + program.getTitle() + "(" + stationName + ")に、" + teamService.getTeamName(teamId) + "が出演します。ぜひご覧ください！";
                tags = tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
            }
            resultMap.put(entry.getKey(), result + "%0A%0A" + tags);
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
        String result = "";
        todayMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getTitle()));
        futureMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getTitle()));

        // 今日の/先1週間の商品ごとの文章を作る(List<商品のテキスト>)
        List<String> todaysElems = blogReleaseItemsText(todayMap);
        List<String> futureElems = blogReleaseItemsText(futureMap);

        // 本日発売の商品
        if (todaysElems.size() > 0) {
            result = String.join("\n\n", String.join("\n\n", todaysElems));
        } else {
            result = "<h2>今日発売の商品はありません。</h2>";
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

        // マスター商品ごとにテキストを作り返却リストに入れる。
        for (Map.Entry<ItemMaster, List<Item>> entry : itemMasterListMap.entrySet()) {
            ItemMaster itemMaster = entry.getKey();
            List<Item> itemList = entry.getValue();

            String date = dateUtils.getDay(itemMaster.getPublication_date());
            String publicationDate = sdf1.format(itemMaster.getPublication_date()) + "(" + date + ")";

            // チーム名が空だった場合正確性に欠けるため、続きの処理には進まず次の商品に進む
            if (!StringUtils.hasText(itemMaster.getTeam_id())) {
                continue;
            }

            List<String> teamNameList = findTeamName(itemMaster.getTeam_id());
            String teamNameUnited = String.join(" ", teamNameList);

            // h2で表示したい商品のタイトルを生成
            String h2 = "";
            // メンバー名もある場合はこちら
            if (StringUtils.hasText(itemMaster.getMember_id()) && !itemMaster.getMember_id().equals("0")) {
                List<String> memberNameList = findMemberName(itemMaster.getMember_id());
                h2 = String.join(" ", publicationDate, teamNameUnited, String.join(" ", memberNameList), itemMaster.getTitle());
            } else {
                // メンバー名ない場合はこちら
                h2 = String.join(" ", publicationDate, teamNameUnited, itemMaster.getTitle());
            }

            // htmlタグ付与
            h2 = "<h2 id=id_" + itemMaster.getItem_m_id() + ">" + h2 + "</h2>";

            String image = StringUtils.hasText(itemMaster.getImage1()) ? "<img src=" + itemMaster.getImage1().replaceAll("\\?.*$", "") + " alt='' />" : "<img src='" + setting.getBlogNoImage() +"' />";

            String description = "<h6>概要</h6>" + "<p>" + itemMaster.getItem_caption() + "</p>";

            Integer estPrice = getPrice(itemList);
            String price = "<h6>価格</h6>" + "<p>" + estPrice + "円</p>";

            String pubDate = sdf1.format(itemMaster.getPublication_date());
            String publicationDateStr = "<h6>発売日</h6>" + "<p>" + pubDate + "</p>";

            String rakutenLink = "<h6>楽天から購入</h6><p>";

            for (Item item : itemList) {
                // 楽天アフィリエイト、ブログカードうまくはまらないので手で作る
                StringBuilder linkCard = new StringBuilder("<a href='" + item.getUrl() + "'><div class='linkCardDiv'><img src=");
                linkCard.append(StringUtils.hasText(item.getImage1()) ? item.getImage1() : setting.getBlogNoImage());
                linkCard.append(" class='linkCardUrl' /><p class='linkCardTitle'>").append(item.getTitle()).append("</p></div></a>");

                // 商品テキストをまとめ、楽天テキストの末尾に加える
                rakutenLink = rakutenLink + "\n" + linkCard.toString();
            }

            String text = String.join("\n", h2, image, description, price, publicationDateStr, rakutenLink);
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

    private List<String> findTeamName(String teamIdListStr) {
        if (teamIdListStr == null || teamIdListStr.equals("")) {
            return null;
        }
        List<Long> teamIdList = List.of(teamIdListStr.split(","))
                .stream().map(Integer::parseInt).collect(Collectors.toList())
                .stream().map(Integer::longValue).collect(Collectors.toList());
        return teamService.findTeamNameByIdList(teamIdList);
    }

    private List<String> findMemberName(String memberIdListStr) {
        if (memberIdListStr == null || memberIdListStr.equals("")) {
            return null;
        }
        List<Long> memberIdList = List.of(memberIdListStr.split(","))
                .stream().map(Integer::parseInt).collect(Collectors.toList())
                .stream().map(Integer::longValue).collect(Collectors.toList());
        return memberService.findMemberNameByIdList(memberIdList);
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

    public String createTitle(Date publicationDate, String title) {
        return  sdf1.format(publicationDate) + " " + title;
    }

    /**
     * TV番組固定ページのテキストを作成。
     * 1日分のみ作成、同じ日のprogramしか渡ってこないはず
     *
     * @param programList
     * @return
     */
    public String tvPageText(List<Program> programList) {
        String result = "[toc depth='5']";
        List<Program> sortedProgramList = programList.stream().sorted(Comparator.comparing(Program::getOn_air_date)).collect(Collectors.toList());

        Date date = DateUtils.localDateTimeToDate(programList.get(0).getOn_air_date());
        result = result + "<br /><h2>" + sdf2.format(date) + "(" +dateUtils.getDay(date) + ")</h2>\n";

        for (Program program : sortedProgramList) {
            String tmp = "";

            List<Long> teamIdList = List.of(program.getTeam_id().split(","))
                    .stream().map(Integer::parseInt).collect(Collectors.toList())
                    .stream().map(Integer::longValue).collect(Collectors.toList());
            List<String> teamNameList = teamService.findTeamNameByIdList(teamIdList);
            String teamName = String.join("/", teamNameList);
            String stationName = stationService.getStationName(program.getStation_id());
            String description = StringUtils.hasText(program.getDescription()) ? program.getDescription() : "";
            tmp = tmp + "<h6>" + dtf1.format(program.getOn_air_date()) + "　" + teamName + "：" + program.getTitle() + "</h6>" + "<br /><p>放送局：" + stationName + "</p><br /><p>番組概要：" + description + "</p>";
            result = String.join("\n", result, tmp);
        }
        return result;
    }
}
