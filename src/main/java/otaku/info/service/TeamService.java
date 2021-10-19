package otaku.info.service;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
     * 引数のStringに含まれるTeamIDをリストにして返却します。
     *
     * @param text
     * @return
     */
    public List<Long> findTeamIdListByText(String text) {
        if (!StringUtils.hasText(text)) {
            return new ArrayList<>();
        }
        List<String> teamNameList = Arrays.stream(TeamEnum.values()).map(TeamEnum::getName).collect(Collectors.toList());
        return teamNameList.stream().filter(e -> text.contains(e) || text.contains(e.replace(" ", ""))).map(e -> TeamEnum.get(e).getId()).collect(Collectors.toList());
    }

    /**
     * 引数のIDを持つチーム名を返します。
     *
     * @param teamId
     * @return
     */
    public String getTeamName(Long teamId) {
        TeamEnum e = TeamEnum.get(teamId);
        if (e != null) {
            return e.getName();
        } else {
            return "";
        }
    }

    /**
     * 引数IDを持つチームのtwitterIdを返します。
     *
     * @param teamId
     * @return
     */
    public String getTwitterId(Long teamId) {
        String result = "";
        TeamEnum e = TeamEnum.get(teamId);
        if (e != null) {
            result = e.getTw_id();
        }
        if (result.equals("")) {
            // デフォルトえびさん
            result = TeamEnum.ABCZ.getTw_id();
        }
        return result;
    }

    /**
     * 引数のIDリストのチーム名を返します。
     *
     * @param teamIdList
     * @return
     */
    public List<String> findTeamNameByIdList(List<Long> teamIdList) {
        return Arrays.stream(TeamEnum.values()).filter(e -> teamIdList.stream().anyMatch(f -> f.equals(e.getId()))).map(TeamEnum::getName).collect(Collectors.toList());
    }

    public List<String> findTwIdListByTeamIdList(List<Long> teamIdList) {
        return Arrays.stream(TeamEnum.values()).filter(e -> teamIdList.stream().anyMatch(f -> e.getId().equals(f))).map(TeamEnum::getTw_id).map(e -> {if (e == null) e = "";
            return "";
        }).distinct().collect(Collectors.toList());
    }

    public Map<Long, String> getTeamIdTwIdMapByTeamIdList(List<Long> teamIdList) {
        Map<Long, String> tmpMap = new HashMap<>();
        Arrays.stream(TeamEnum.values()).filter(e -> teamIdList.stream().anyMatch(f -> e.getId().equals(f))).forEach(e -> tmpMap.put(e.getId(), e.getTw_id()));
        Map<Long, String> resultMap = new HashMap<>();
        for (Map.Entry<Long, String> e : tmpMap.entrySet()) {
            if (!resultMap.containsKey(e.getValue())) {
                resultMap.put(e.getKey(), e.getValue());
            }
        }
        return resultMap;
    }
}
