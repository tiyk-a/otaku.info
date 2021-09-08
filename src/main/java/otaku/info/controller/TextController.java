package otaku.info.controller;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import otaku.info.dto.TeamIdMemberNameDto;
import otaku.info.dto.TwiDto;
import otaku.info.dto.WpDto;
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

    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年M月d日");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("M/d");
    private SimpleDateFormat sdf3 = new SimpleDateFormat("h:m");
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");

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
     * TV出演情報がないグループのTwitter投稿文
     *
     * @param teamId Teamテーブルのteam_id
     * @param forToday 今日の情報(TRUE)、明日の情報(FALSE)
     * @param date 情報の日付
     * @return
     */
    public String tvPostNoAlert(Long teamId, boolean forToday, Date date) {
        String dateStr = forToday ? "今日(" + sdf2.format(date) + ")" : "明日(" + sdf2.format(date) + ")";
        String tags = tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
        return dateStr + "の" + teamService.getTeamName(teamId) + "のTV出演情報はありません。" + "%0A%0A" + tags;
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
            info = info + sdf3.format(p.getOn_air_date()) + " " + p.getTitle() + " (" + stationService.getStationName(p.getStation_id()) + ")%0A";
        }
        return result + info;
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
            if (program.getMember_id().contains(".")) {
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
            String formattedDateTime = program.getOn_air_date().format(formatter);

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
     * @param todaysItems
     * @param futureItems
     * @return
     */
    public String blogUpdateReleaseItems(List<Item> todaysItems, List<Item> futureItems) {
        String result = "";

        List<String> todaysElems = blogReleaseItemsText(todaysItems);
        List<String> futureElems = blogReleaseItemsText(futureItems);

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
     * @param itemList
     * @return
     */
    private List<String> blogReleaseItemsText(List<Item> itemList) {
        List<String> resultList = new ArrayList<>();

        for (Item item : itemList) {
            String date = dateUtils.getDay(item.getPublication_date());
            String publicationDate = sdf2.format(item.getPublication_date()) + "(" + date + ")";

            // チーム名が空だった場合正確性に欠けるため、続きの処理には進まず次の商品に進む
            if (!StringUtils.hasText(item.getTeam_id())) {
                continue;
            }

            List<String> teamNameList = findTeamName(item.getTeam_id());
            String teamNameUnited = String.join(" ", teamNameList);

            // h2で表示したい商品のタイトルを生成
            String h2 = "";
            // メンバー名もある場合はこちら
            if (StringUtils.hasText(item.getMember_id()) && !item.getMember_id().equals("0")) {
                List<String> memberNameList = findMemberName(item.getMember_id());
                h2 = String.join(" ", publicationDate, teamNameUnited, String.join(" ", memberNameList));
            } else {
                // メンバー名ない場合はこちら
                h2 = String.join("", publicationDate, teamNameUnited);
            }

            // htmlタグ付与
            h2 = "<h2 id=id_" + item.getItem_id() + ">" + h2 + "</h2>";

            // h3を生成
            String h3 = "<h3>" + item.getTitle() + "</h3>";

            String image1 = StringUtils.hasText(item.getImage1()) ? "<a href=" + item.getUrl() + "><img src=" + item.getImage1().replaceAll("\\?.*$", "") + " alt='' /></a>" : "";

            String aHref = "<a href=" + item.getUrl() + ">" + item.getTitle() +"</a>";

            String image2 = StringUtils.hasText(item.getImage2()) ? "<a href=" + item.getUrl() + "><img src=" + item.getImage2().replaceAll("\\?.*$", "") + " alt='' /></a>" : "";

            String image3 = StringUtils.hasText(item.getImage3()) ? "<a href=" + item.getUrl() + "><img src=" + item.getImage3().replaceAll("\\?.*$", "") + " alt='' /></a>" : "";

            String p = "<p>" + item.getItem_caption() + "</p>";

            // 商品単体ページのリンク
            String itemUrl = "";
            if (item.getWp_id() != null) {
                itemUrl = "<a href=https://otakuinfo.fun/item/" + item.getWp_id() + ">商品紹介ページはこちら</a>";
            }

            String text = String.join("\n", h2, h3, image1, aHref, image2, image3, p, itemUrl);

            // 返却リストに追加する
            resultList.add(text);
        }
        return resultList;
    }

    /**
     * 商品ブログ投稿文章
     * Key: タイトル
     * Value: テキスト
     *
     * @param item
     * @return
     */
    public WpDto blogItemText(Item item) {
        WpDto wpDto = new WpDto();

        String publicationDate;
        if (item.getPublication_date() != null) {
            String date = dateUtils.getDay(item.getPublication_date());
            publicationDate = sdf2.format(item.getPublication_date()) + "(" + date + ")";
        } else {
            item.setFct_chk(false);
            itemService.saveItem(item);
            return null;
        }

        // チーム名が空だった場合正確性に欠けるため、続きの処理には進まず次の商品に進む
        if (!StringUtils.hasText(item.getTeam_id())) {
            return null;
        }

        List<String> teamNameList = findTeamName(item.getTeam_id());
        String teamNameUnited = "【" + String.join(" ", teamNameList) + "】";

        // h2で表示したい商品のタイトルを生成
        String h2 = "";
        // メンバー名もある場合はこちら
        if (StringUtils.hasText(item.getMember_id()) && !item.getMember_id().equals("0")) {
            List<String> memberNameList = findMemberName(item.getMember_id());
            h2 = String.join(" ", publicationDate, teamNameUnited, String.join(" ", memberNameList), item.getTitle());
        } else {
            // メンバー名ない場合はこちら
            h2 = String.join(" ", publicationDate, teamNameUnited, item.getTitle());
        }

        wpDto.setTitle(h2);

        // h3を生成
        String h3 = "<h3>" + item.getTitle() + "</h3>";

        String image1 = StringUtils.hasText(item.getImage1()) ? "<a href=" + item.getUrl() + "><img src=" + item.getImage1().replaceAll("\\?.*$", "") + " alt='' /></a>" : "";

        String aHref = "<a href=" + item.getUrl() + ">" + item.getTitle() +"</a>";

        String image2 = StringUtils.hasText(item.getImage2()) ? "<a href=" + item.getUrl() + "><img src=" + item.getImage2().replaceAll("\\?.*$", "") + " alt='' /></a>" : "";

        String image3 = StringUtils.hasText(item.getImage3()) ? "<a href=" + item.getUrl() + "><img src=" + item.getImage3().replaceAll("\\?.*$", "") + " alt='' /></a>" : "";

        String p = "<p>" + item.getItem_caption() + "</p>";

        // 続くp要素を生成
        String text = String.join("\n", h3, image1, image2, image3, aHref, p);
        wpDto.setContent(text);
        return wpDto;
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

    /**
     * 商品マスターのブログ投稿のため、各商品のリンクなどを含むページコンテンツを編集、Stringを返却します
     *
     * @param itemList
     * @return
     */
    public String createBlogContent(List<Item> itemList, String content) {
        // 商品マスターのコンテンツをベースとする。
        String result = content;
        for (Item item : itemList) {
            String title = "<h3>" + item.getTitle() + "</h3>";
            String image1 = StringUtils.hasText(item.getImage1()) ? "<a href=" + item.getUrl() + "><img src=" + item.getImage1().replaceAll("\\?.*$", "") + " alt='' /></a>" : "";
            String image2 = StringUtils.hasText(item.getImage2()) ? "<a href=" + item.getUrl() + "><img src=" + item.getImage2().replaceAll("\\?.*$", "") + " alt='' /></a>" : "";
            String image3 = StringUtils.hasText(item.getImage3()) ? "<a href=" + item.getUrl() + "><img src=" + item.getImage3().replaceAll("\\?.*$", "") + " alt='' /></a>" : "";
            String p = "<p>" + item.getItem_caption() + "</p>";

            // 続くp要素を生成
            String text = String.join("\n", title, image1, image2, image3, p);

            if (StringUtils.hasText(result)) {
                result = result + "\n" + text;
            } else {
                result = text;
            }
        }
        return result;
    }
}
