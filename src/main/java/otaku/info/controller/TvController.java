package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.entity.Program;
import otaku.info.entity.Station;
import otaku.info.searvice.MemberService;
import otaku.info.searvice.ProgramService;
import otaku.info.searvice.StationService;
import otaku.info.searvice.TeamService;

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
            if (p.getTeam_id() != null && !p.getTeam_id().equals("")) {
                List.of(p.getTeam_id().split(",")).forEach(e -> teamIdList.add((long) Integer.parseInt(e)));
            }
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
    public void tvKingdomSave(Map<String, String[]> detailTitleMap, String teamName) throws IOException {
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

            // 取得テキスト（TV情報・タイトル）からチーム名を抽出する
            List<Long> teamIdList = teamService.findTeamIdListByText(valueArr[0]);

            // 取得テキスト（TV情報・タイトル）からチーム名を取れなかったら（TV情報・詳細）からとる
            if (teamIdList.size() == 0) {
                teamIdList.addAll(teamService.findTeamIdListByText(e.getKey()));
            }

            // 取得テキスト（TV情報・タイトル）からチーム名を取れなかったら（TV情報・詳細画面）からとる
            if (teamIdList.size() == 0) {
                String[] detail = getDetails(valueArr[1]);

                // 番組概要からチーム名を取得
                teamIdList.addAll(teamService.findTeamIdListByText(detail[0]));

                // 番組概要からチーム名を取得
                if (teamIdList.size() == 0) {
                    teamIdList.addAll(teamService.findTeamIdListByText(detail[1]));
                }
            }

            // 取得テキスト（TV情報・タイトルと詳細と詳細情報画面）からチーム名取れなかったら引数からとる
            if (teamIdList.size() == 0) {
                teamIdList.addAll(teamService.findTeamIdListByText(teamName));
            }

            String teamIdStr = StringUtils.join(teamIdList, ',');
            program.setTeam_id(teamIdStr);

            // 取得テキスト（TV情報・タイトル）からメンバー名を抽出する
            List<Long> memberIdList = memberService.findMemberIdByText(valueArr[0]);

            // 取得テキスト（TV情報・タイトル）からメンバー名を取れなかったら（TV情報・詳細）からとる
            if (memberIdList.size() == 0) {
                memberIdList.addAll(memberService.findMemberIdByText(e.getKey()));
            }

            // 取得テキスト（TV情報・タイトル）からメンバー名を取れなかったら（TV情報・詳細画面）からとる
            if (memberIdList.size() == 0) {
                String[] detail = getDetails(valueArr[1]);

                // 番組概要からメンバー名を取得
                memberIdList.addAll(memberService.findMemberIdByText(detail[0]));

                // 番組概要からメンバー名を取得
                if (memberIdList.size() == 0) {
                    memberIdList.addAll(memberService.findMemberIdByText(detail[1]));
                }
            }

            if (memberIdList.size() > 0) {
                String memberIdStr = StringUtils.join(memberIdList, ',');
                program.setMember_id(memberIdStr);
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
                break;
            }

            // 空レコードを登録しないようにします。
            if (program.getTitle().equals("") && program.getDescription().equals("") && program.getOn_air_date() == null) {
                break;
            }

            // 既存データに重複がないか比較する
            boolean isExisting = programService.hasProgram(program.getTitle(), program.getStation_id(), program.getOn_air_date());

            // 登録or更新処理
            if (isExisting) {
                // すでに登録があったら内容を比較し、違いがあれば更新
                Program existingP = programService.findByIdentity(program.getTitle(), program.getStation_id(), program.getOn_air_date());
                boolean isTotallySame = isTotallySame(existingP, program);

                // 完全に同じデータの場合、登録・更新どちらも行わない。
                // 更新
                if (!isTotallySame || (existingP.getTeam_id().equals("")) && !program.getTeam_id().equals("")) {

                    programService.overwrite(existingP.getProgram_id(), program);
                    logger.info("TV番組を更新：" + program.toString());
                }
            } else {
                // 既存登録がなければ新規登録します。
                programService.save(program);
                logger.info("TV番組を登録：" + program.toString());
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
     * @param program
     * @return
     */
    private boolean isTotallySame(Program existingP, Program program) {

        // member_id
        if (existingP.getMember_id() == null) {
            if (program.getMember_id() != null) {
                return false;
            }
        } else {
            if (!existingP.getMember_id().equals(program.getMember_id())) {
                return false;
            }
        }

        // team_id
        if (existingP.getTeam_id() == null) {
            if (program.getTeam_id() != null) {
                return false;
            }
        } else {
            if (!existingP.getTeam_id().equals(program.getTeam_id())) {
                return false;
            }
        }

        // description
        if (existingP.getDescription() == null) {
            return program.getDescription() == null;
        } else {
            if (!existingP.getDescription().equals(program.getDescription())) {
                return false;
            }
        }
        return true;
    }
}
