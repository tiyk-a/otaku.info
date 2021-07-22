package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.entity.Program;
import otaku.info.searvice.ProgramService;
import otaku.info.searvice.TeamService;

import java.util.*;

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
     * （「各グループ名＋出演情報ありません」の投稿）
     *
     * @param forToday
     * @param date
     */
    public void allNoTvPost(boolean forToday, Date date) throws JSONException {
        List<Long> teamList = teamService.getAllId();
        for (Long teamId : teamList) {
            pythonController.post(teamId.intValue(), textController.tvPostNoAlert(teamId, forToday, date));
        }
    }
}
