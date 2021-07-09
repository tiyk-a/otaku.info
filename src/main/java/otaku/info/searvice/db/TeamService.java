package otaku.info.searvice.db;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Team;
import otaku.info.repository.TeamRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public List<Team> findAllTeam() {
        return teamRepository.findAll();
    }
}
