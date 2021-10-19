package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.entity.PRelMem;
import otaku.info.entity.Program;
import otaku.info.entity.PRel;
import otaku.info.entity.Station;
import otaku.info.enums.TeamEnum;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;

import java.io.IOException;
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

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("TvController");

    @Autowired
    private final ProgramService programService;

    @Autowired
    private final TeamService teamService;

    @Autowired
    private final TextController textController;

    @Autowired
    private final PythonController pythonController;

    @Autowired
    private final MemberService memberService;

    @Autowired
    private final StationService stationService;

    @Autowired
    private final PRelService pRelService;

    @Autowired
    private PRelMemService pRelMemService;

    private static org.springframework.util.StringUtils StringUtilsSpring;

    final Pattern datePattern = Pattern.compile("^[0-9][0-9]?/[0-9][0-9]? \\((Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\) [0-9][0-9]?:[0-9][0-9]?");

    final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("M/d (EEE) H:m")
            .parseDefaulting(ChronoField.YEAR, Calendar.getInstance().get(Calendar.YEAR)).toFormatter(Locale.US);

    /**
     * 放送日から番組を検索し返却します。
     *
     * @param date
     * @return
     */
    public List<Program> getTvList(Date date) {
        return programService.findByOnAirDate(date);
    }

    /**
     * 番組リストをグループごとにマップして返却します。
     *
     * @param programList
     * @return
     */
    public Map<Long, List<Program>>  mapByGroup(List<Program> programList) {
        Map<Long, List<Program>> tvListMapByGroup = new HashMap<>();

        // 全グループIDを取得して、それぞれを空プログラムリストを値としてMapに入れる。Mapサイズはここで完成。
        Arrays.stream(TeamEnum.values()).map(TeamEnum::getId).forEach(e -> tvListMapByGroup.put(e, new ArrayList<>()));

        // マップのvalueに情報を追加していく
        for (Program p : programList) {
            // マップからグループIDの要素のvalueに情報を追加して
            List<Long> teamIdList = pRelService.getTeamIdList(p.getProgram_id());

            if (teamIdList != null && !teamIdList.isEmpty()) {
                for (Long teamId : teamIdList) {
                    List<Program> list = tvListMapByGroup.get(teamId);
                    if (list != null && !list.isEmpty()) {
                        list.add(p);
                        tvListMapByGroup.put(teamId, list);
                    }
                }
            }
        }

        return tvListMapByGroup;
    }

    /**
     * tvkingdomから取得したTV情報を分解して保存します。
     *
     * @param detailTitleMap
     */
    public void tvKingdomSave(Map<String, String[]> detailTitleMap, String teamName, Long memId) throws IOException {
        Long teamId = TeamEnum.get(teamName).getId();

        for (Map.Entry<String, String[]> e : detailTitleMap.entrySet()) {
            String[] valueArr = e.getValue();

            // 新しいProjectオブジェクトを作ります。
            Program program = new Program();
            program.setTitle(valueArr[0]);
            program.setFct_chk(false);

            Matcher m = datePattern.matcher(e.getKey());
            if (m.find()) {
                program.setOn_air_date(LocalDateTime.parse(m.group(), dateFormatter));
            }

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
            // アダルトサイトのデータが引っかかっていた場合、この先の処理は行わず削除してreturn
            if (program.getStation_id() == 16) {
                continue;
            }

            // 空レコードを登録しないようにします。
            if (program.getTitle().equals("") && program.getDescription().equals("") && program.getOn_air_date() == null) {
                continue;
            }

            // 既存データに重複がないか比較する
            boolean isExisting = programService.hasProgram(program.getTitle(), program.getStation_id(), program.getOn_air_date());

            // 登録or更新処理
            if (isExisting) {
                // 内容が同じProgramを取得
                Program existingP = programService.findByIdentity(program.getTitle(), program.getStation_id(), program.getOn_air_date());
                PRel rel = pRelService.findByProgramIdTeamId(existingP.getProgram_id(), teamId);
                if (rel == null) {
                    PRel newRel = new PRel(null, existingP.getProgram_id(), teamId, null, null);
                    rel = pRelService.save(newRel);
                }

                if (memId != null) {
                    List<PRelMem> relMemList = pRelMemService.findByPRelId(rel.getP_rel_id());
                    boolean existsMem = relMemList.stream().anyMatch(f -> f.getMember_id().equals(memId));
                    if (!existsMem) {
                        PRelMem newMem = new PRelMem(null, rel.getP_rel_id(), memId, null, null);
                        pRelMemService.save(newMem);
                    }
                }
            } else {
                // 既存登録がなければ新規登録します。
                Program savedP = programService.save(program);
                PRel rel = new PRel(null, savedP.getProgram_id(), teamId, null, null);
                PRel savedRel = pRelService.save(rel);
                PRelMem relMem = new PRelMem(null, savedRel.getP_rel_id(), memId, null, null);
                pRelMemService.save(relMem);
                logger.debug("TV番組を登録：" + program.toString());
            }
        }
    }

    /**
     * 詳細画面の内容を取得する
     *
     * @param url
     * @return
     * @throws IOException
     */
    private String[] getDetails(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        // 必要な要素を取り出す
        Elements elements = document.select("div.contBlock.subUtileSetting");
        String[] resultArr = new String[2];

        for (Element e : elements) {
            if (e.getElementsByTag("h3").text().equals("番組概要")) {
                resultArr[0] = e.getElementsByTag("p").first().getElementsByClass("basicTxt").text();
            }
            if (e.getElementsByTag("h3").text().equals("番組詳細")) {
                resultArr[1] = e.getElementsByTag("p").first().getElementsByClass("basicTxt").text();
            }
        }
        return resultArr;
    }

    /**
     * 2つの番組が完全に同じか確認する。
     * Title, Station_id, On_air_dateで比較元番組を取得しているため、この3項目はこのメソッド内でチェックしない。
     *
     * @param existingP
     * @param description
     * @param teamIdList
     * @param memberIdList
     * @return
     */
    private boolean isTotallySame(Program existingP, String description, List<Long> teamIdList, List<Long> memberIdList) {

        // 簡単なものから確認してreturnしていく
        // description
        if (existingP.getDescription() == null) {
            if (description != null) {
                return false;
            }
        } else {
            if (!existingP.getDescription().equals(description)) {
                return false;
            }
        }

        // member_id
        List<Long> eMemberIdList = pRelService.getMemberIdList(existingP.getProgram_id());
        if (memberIdList == null) {
            if (eMemberIdList != null) {
                return false;
            }
        } else {
            if (eMemberIdList == null || memberIdList.size() != eMemberIdList.size()) {
                return false;
            }
            for (Long memberId : memberIdList) {
                if (eMemberIdList.stream().noneMatch(e -> e.equals(memberId))) {
                    return false;
                }
            }
        }

        // team_id
        List<Long> eTeamIdList = pRelService.getTeamIdList(existingP.getProgram_id());
        if (teamIdList == null) {
            return eTeamIdList == null;
        } else {
            if (eTeamIdList == null || teamIdList.size() != eTeamIdList.size()) {
                return false;
            }
            for (Long teamId : teamIdList) {
                if (eTeamIdList.stream().noneMatch(e -> e.equals(teamId))) {
                    return false;
                }
            }
        }
        return true;
    }
}
