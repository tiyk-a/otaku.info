package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.entity.Program;
import otaku.info.entity.Station;
import otaku.info.searvice.MemberService;
import otaku.info.searvice.ProgramService;
import otaku.info.searvice.StationService;
import otaku.info.searvice.TeamService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@AllArgsConstructor
public class TvController  {

    @Autowired
    private ProgramService programService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TextController textController;

    @Autowired
    private PythonController pythonController;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StationService stationService;

    final Pattern datePattern = Pattern.compile("^[0-9][0-9]?/[0-9][0-9]? \\((Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\) [0-9][0-9]?:[0-9][0-9]?");

//    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d (EEE) H:m");

    final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("M/d (EEE) H:m")
            .parseDefaulting(ChronoField.YEAR, Calendar.getInstance().get(Calendar.YEAR)).toFormatter(Locale.US);

    final Logger logger = org.slf4j.LoggerFactory.getLogger(TvController.class);

    public List<Program> getTvList(Date date) {
        return programService.findByOnAirDate(date);
    }

    public Map<Long, List<Program>>  mapByGroup(List<Program> programList) {
        Map<Long, List<Program>> tvListMapByGroup = new HashMap<>();

        // 全グループIDを取得して、それぞれを空プログラムリストを値としてMapに入れる。Mapサイズはここで完成。
        teamService.getAllId().forEach(e -> tvListMapByGroup.put(e, new ArrayList<>()));

        // マップのvalueに情報を追加していく
        for (Program p : programList) {
            // マップからグループIDの要素のvalueに情報を追加して
            List<Long> teamIdList = new ArrayList<>();
            List.of(p.getTeam_id().split(",")).forEach(e -> teamIdList.add(Long.valueOf(Integer.valueOf(e))));

            for (Long teamId : teamIdList) {
                List<Program> list = tvListMapByGroup.get(teamId);
                list.add(p);
                tvListMapByGroup.put(teamId, list);
            }
        }

        return tvListMapByGroup;
    }

    /**
     * 全てのグループでTV出演情報がない時の処理
     * （Twitterのあるグループのみ「グループ名＋出演情報ありません」の投稿）
     *
     * @param forToday
     * @param date
     */
    public void allNoTvPost(boolean forToday, Date date) throws JSONException {
        List<Long> teamList = teamService.getIdByTw();
        for (Long teamId : teamList) {
            pythonController.post(teamId.intValue(), textController.tvPostNoAlert(teamId, forToday, date));
        }
    }

    /**
     * tvkingdomから取得したTV情報を分解して保存します。
     *
     * @param detailTitleMap
     */
    public void tvKingdomSave(Map<String, String> detailTitleMap, String teamName) {
        for (Map.Entry<String, String> e : detailTitleMap.entrySet()) {

            // 新しいProjectオブジェクトを作ります。
            Program program = new Program();
            program.setTitle(e.getValue());
            program.setFct_chk(false);

            Matcher m = datePattern.matcher(e.getKey());
            if (m.find()) {
                program.setOn_air_date(LocalDateTime.parse(m.group(), dateFormatter));
            }

            List<Long> teamIdList = teamService.findTeamIdListByText(e.getValue());
            if (teamIdList.size() == 0) {
                teamIdList.addAll(teamService.findTeamIdListByText(e.getValue()));
            }
            teamIdList.addAll(teamService.findTeamIdListByText(e.getKey()));
            String teamIdStr = StringUtils.join(teamIdList, ',');
            program.setTeam_id(teamIdStr);

            List<Long> memberIdList = memberService.findMemberIdByText(e.getValue());
            memberIdList.addAll(memberService.findMemberIdByText(e.getKey()));
            String memberIdStr = StringUtils.join(memberIdList, ',');
            program.setMember_id(memberIdStr);

            String station = e.getKey().replaceAll("^.*\\([0-9]*分\\) ", "");
            String station2 = station.replaceAll("\\(Ch.*", "");
            Long stationId = stationService.findStationId(station2).orElse(null);
            if (stationId != null) {
                program.setStation_id(stationId);
            } else {
                Station s = new Station();
                s.setStation_name(station2);
                if (station2.length() > 8) {
                    s.setKeyword(station2.substring(0, 9));
                } else {
                    s.setKeyword(station2);
                }
                Station savedStation = stationService.save(s);
                program.setStation_id(savedStation.getStation_id());
            }
            program.setDescription(e.getKey());

            // Programの内容を精査します。
            // 既存データに重複がないか比較する
            boolean isExisting = programService.hasProgram(program.getTitle(), program.getStation_id(), program.getOn_air_date());
            logger.info(program.toString());
            if (isExisting) {
                // すでに登録があったら内容を比較し、違いがあれば更新
                Program existingP = programService.findByIdentity(program.getTitle(), program.getStation_id(), program.getOn_air_date());
                boolean isTotallySame = existingP.getMember_id().equals(program.getMember_id()) && existingP.getTeam_id().equals(program.getTeam_id()) &&
                        existingP.getDescription().equals(program.getDescription());
                if (!isTotallySame) {
                    programService.overwrite(existingP.getProgram_id(), program);
                }
            } else {
                // 既存登録がなければ新規登録します。
                programService.save(program);
            }
        }
    }
}
