package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.entity.*;
import otaku.info.enums.MemberEnum;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
import otaku.info.utils.StringUtilsMine;

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
    private final PMService pmService;

    @Autowired
    private final StationService stationService;

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
    public List<PM> getTvList(Date date) {
        return pmService.findByOnAirDateNotDeleted(date);
    }

    /**
     * PMリストをteamIdごとにマップして返却します。
     *
     * @param pmList
     * @return
     */
    public Map<Long, List<PM>>  mapByGroup(List<PM> pmList) {
        // teamId, List<PM>
        Map<Long, List<PM>> tvListMapByGroup = new HashMap<>();

        // マップのvalueに情報を追加していく
        for (PM pm : pmList) {
            // マップからグループIDの要素のvalueに情報を追加して
            List<Long> teamIdList = StringUtilsMine.stringToLongList(pm.getTeamArr());

            if (teamIdList != null && !teamIdList.isEmpty()) {
                for (Long teamId : teamIdList) {
                    List<PM> tmpList;
                    if (tvListMapByGroup.containsKey(teamId)) {
                        tmpList = tvListMapByGroup.get(teamId);
                    } else {
                        tmpList = new ArrayList<>();
                    }
                    tmpList.add(pm);
                    tvListMapByGroup.put(teamId, tmpList);
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
    public void tvKingdomSave(Map<String, String[]> detailTitleMap, Long teamId, Long memId) {

        if (teamId == null) {
            teamId = MemberEnum.get(memId).getTeamId();
        }

        for (Map.Entry<String, String[]> e : detailTitleMap.entrySet()) {
            String[] valueArr = e.getValue();

            // 新しいProjectオブジェクトを作ります。
            Program program = new Program();
            program.setTitle(valueArr[0]);
            program.setUrl(valueArr[1]);
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
                s.setDel_flg(false);
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
                String teamArr = existingP.getTeamArr();
                teamArr = StringUtilsMine.addToStringArr(teamArr, teamId);
                existingP.setTeamArr(StringUtilsMine.removeBrackets(teamArr));

                String memArr = existingP.getMemArr();
                memArr = StringUtilsMine.addToStringArr(memArr, memId);
                existingP.setMemArr(StringUtilsMine.removeBrackets(memArr));
                programService.save(existingP);
            } else {
                // 既存登録がなければ新規登録します。
                program.setTeamArr(StringUtilsMine.removeBrackets(teamId.toString()));
                program.setMemArr(StringUtilsMine.removeBrackets(memId.toString()));
                programService.save(program);
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
}
