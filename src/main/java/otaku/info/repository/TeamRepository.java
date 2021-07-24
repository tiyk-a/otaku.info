package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Team;

import java.util.List;

/**
 * グループテーブルのrepository
 *
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT mnemonic FROM Team WHERE team_name = ?1")
    String getMnemonic(String teamName);

    @Query("SELECT a.team_id FROM Team a WHERE ?1 LIKE CONCAT('%', a.team_name, '%') OR ?1 LIKE CONCAT('%', TRIM(replace(a.team_name, ' ','')), '%')")
    List<Long> findTeamIdListByText(String text);

    @Query("SELECT team_id FROM Team")
    List<Long> getAllId();

    @Query("SELECT team_id FROM Team WHERE tw_id IS NOT NULL")
    List<Long> getIdByTw();

    @Query("SELECT team_name FROM Team WHERE team_id = ?1")
    String findTeamNameById(Long teamId);
}
