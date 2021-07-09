package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import otaku.info.entity.Team;

/**
 * グループテーブルのrepository
 *
 */
public interface TeamRepository extends JpaRepository<Team, Long> {
}
