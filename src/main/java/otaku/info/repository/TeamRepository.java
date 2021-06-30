package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import otaku.info.entity.Item;
import otaku.info.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
