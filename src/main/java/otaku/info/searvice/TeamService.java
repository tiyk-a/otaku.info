package otaku.info.searvice;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Team;
import otaku.info.enums.TeamEnum;

import javax.transaction.Transactional;

/**
 * グループテーブルのサービス
 *
 */
@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class TeamService {

    /**
     * 全チームデータを返します。
     *
     * @return
     */
    public List<Team> findAllTeam() {
        return Arrays.stream(TeamEnum.values()).map(TeamEnum::convertToEntity).collect(Collectors.toList());
    }

    /**
     * 全チームデータのチーム名のみを返します。
     *
     * @return
     */
    public List<String> findAllTeamName() {
        return Arrays.stream(TeamEnum.values()).map(TeamEnum::getName).collect(Collectors.toList());
    }

    /**
     * チーム名からmnemonicを返します。
     *
     * @param teamName
     * @return
     */
    public String getMnemonic(String teamName) {
        return TeamEnum.get(teamName).getName();
    }

    /**
     * 引数のStringに含まれるTeamIDをリストにして返却します。
     *
     * @param text
     * @return
     */
    public List<Long> findTeamIdListByText(String text) {
        List<String> teamNameList = findAllTeamName();
        return teamNameList.stream().filter(e -> text.contains(e) || text.contains(e.replace(" ", ""))).map(e -> (long) TeamEnum.get(e).getId()).collect(Collectors.toList());
    }

    /**
     * 全てのチームのIDを返します。
     *
     * @return
     */
    public List<Long> getAllId() {
        return Arrays.stream(TeamEnum.values()).map(e -> (long) e.getId()).collect(Collectors.toList());
    }

    /**
     * 引数のIDを持つチーム名を返します。
     *
     * @param teamId
     * @return
     */
    public String getTeamName(Long teamId) {
        return TeamEnum.get(Math.toIntExact(teamId)).getName();
    }

//    /**
//     * manage画面で使用、今画面見ないので未対応
//     *
//     * @return
//     */
//    public Map<Long, String> getAllIdNameMap() {
//        List<String> list = teamRepository.getAllIdNameMap();
//        Map<Long, String> resultMap = new HashMap<>();
//        for (String s: list) {
//            String[] elem = s.split("_");
//            resultMap.put(Long.parseLong(elem[0]),elem[1]);
//        }
//        return resultMap;
//    }

    /**
     * 引数IDを持つチームのtwitterIdを返します。
     *
     * @param teamId
     * @return
     */
    public String getTwitterId(Long teamId) {
        return TeamEnum.get(Math.toIntExact(teamId)).getTw_id();
    }

    /**
     * 引数のIDリストのチーム名を返します。
     *
     * @param teamIdList
     * @return
     */
    public List<String> findTeamNameByIdList(List<Long> teamIdList) {
        return Arrays.stream(TeamEnum.values()).filter(e -> teamIdList.stream().anyMatch(f -> f.equals((long) e.getId()))).map(TeamEnum::getName).collect(Collectors.toList());
    }
}
