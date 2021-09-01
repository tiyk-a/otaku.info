package otaku.info.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.dto.TeamIdMemberNameDto;
import otaku.info.dto.TwiDto;
import otaku.info.entity.*;
import otaku.info.searvice.MemberService;
import otaku.info.searvice.StationService;
import otaku.info.searvice.TagService;
import otaku.info.searvice.TeamService;
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
    private TeamService teamService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StationService stationService;

    @Autowired
    private TagService tagService;

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

    /**
     * 未来発売商品のリマインドのための文章を作ります
     * batch/2
     * 
     * @param twiDto
     * @return
     */
    public String futureItemReminder(TwiDto twiDto) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        int diff = dateUtils.dateDiff(today.getTime(), twiDto.getPublication_date()) + 1;
        String tags = tagService.getTagByTeam(twiDto.getTeam_id()).stream().collect(Collectors.joining(" #","#",""));
        return "【PR 発売まで" + diff + "日】%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl() + "%0A%0A" + tags;
    }

    public String futureItemReminder(TwiDto twiDto, List<Long> teamIdList) {
        int diff = dateUtils.dateDiff(new Date(), twiDto.getPublication_date());
        String tags = "";
        for (Long teamId : teamIdList) {
            tags = tags + " " + tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
        }
        return "【PR 発売まで" + diff + "日】%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl() + "%0A%0A" + tags;
    }

    public String releasedItemAnnounce(Item item) {
        String str1 = "【PR】本日発売！%0A%0A" + item.getTitle() + "%0A%0A";
        String str2 = item.getUrl();
        String result;
        int length = str1.length() + str2.length() + "%0A%0A".length();
        if (length < 140) {
            result = str1 + item.getItem_caption().substring(0, 140 - length) + "%0A%0A" + str2;
        } else {
            result = str1 + str2;
        }
        String tags = tagService.getTagByTeam(Long.parseLong(item.getTeam_id())).stream().collect(Collectors.joining(" #","#",""));
        return result + "%0A%0A" + tags;
    }

    public String releasedItemAnnounce(Item item, List<Long> teamIdList) {
        String str1 = "【PR】本日発売！%0A%0A" + item.getTitle() + "%0A%0A";
        String str2 = item.getUrl();
        String result;
        int length = str1.length() + str2.length() + "%0A%0A".length();
        if (length < 140) {
            result = str1 + item.getItem_caption().substring(0, 140 - length) + "%0A%0A" + str2;
        } else {
            result = str1 + str2;
        }

        String tags = "";
        for (Long teamId : teamIdList) {
            tags = tags + " " + tagService.getTagByTeam(teamId).stream().collect(Collectors.joining(" #","#",""));
        }

        return result + "%0A%0A" + tags;
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
}
