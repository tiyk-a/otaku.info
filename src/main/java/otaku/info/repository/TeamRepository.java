package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Team;

/**
 * グループテーブルのrepository
 *
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT mnemonic FROM Team WHERE team_name = ?1")
    String getMnemonic(String teamName);
}
