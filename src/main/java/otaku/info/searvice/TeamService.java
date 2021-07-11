package otaku.info.searvice;

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

    public String getMnemonic(String teamName) {
        return teamRepository.getMnemonic(teamName);
    }
}
