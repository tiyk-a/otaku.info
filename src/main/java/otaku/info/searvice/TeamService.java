package otaku.info.searvice;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Team;
import otaku.info.repository.TeamRepository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * グループテーブルのサービス
 *
 */
@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public List<Team> findAllTeam() {
        return teamRepository.findAll();
    }

    public List<String> findAllTeamName() {
        return teamRepository.findAllTeamName();
    }
    public String getMnemonic(String teamName) {
        return teamRepository.getMnemonic(teamName);
    }

    public List<Long> findTeamIdListByText(String text) {
        return teamRepository.findTeamIdListByText(text);
    }

    public List<Long> getAllId() {
        return teamRepository.getAllId();
    }

    /**
     * TwitterのIDがあるグループのIDを取得する
     *
     * @return
     */
    public List<Long> getIdByTw() {
        return teamRepository.getIdByTw();
    }

    public String getTeamName(Long teamId) {
        return teamRepository.findTeamNameById(teamId);
    }

    public Map<Long, String> getAllIdNameMap() {
        List<String> list = teamRepository.getAllIdNameMap();
        Map<Long, String> resultMap = new HashMap<>();
        for (String s: list) {
            String[] elem = s.split("_");
            resultMap.put(Long.parseLong(elem[0]),elem[1]);
        }
        return resultMap;
    }
}
