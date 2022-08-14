package otaku.info.service;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
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
    public Map<String, Long> findTeamNameByIdList(List<Long> teamIdList) {
        return Arrays.stream(TeamEnum.values()).filter(e -> teamIdList.stream().anyMatch(f -> f.equals(e.getId()))).collect(Collectors.toMap(TeamEnum::getName, TeamEnum::getId));
    }

    public List<String> findTwIdListByTeamIdList(List<Long> teamIdList) {
        return Arrays.stream(TeamEnum.values()).filter(e -> teamIdList.stream().anyMatch(f -> e.getId().equals(f))).map(TeamEnum::getTw_id).map(e -> {if (e == null) e = "";
            return "";
        }).distinct().collect(Collectors.toList());
    }
}
