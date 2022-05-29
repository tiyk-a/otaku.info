package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PM;

import java.util.List;

public interface PMRepository extends JpaRepository<PM, Long> {

    /**
     * 未来のpmで有効なものだけ取ってくる
     *
     * @param pmId
     * @return
     */
    @Query(nativeQuery = true, value = "select a.* from pm a inner join pm_ver b on a.pm_id = b.pm_id inner join pm_rel c on a.pm_id = c.pm_id where c.team_id = ?1 and b.on_air_date >= CURRENT_DATE and a.del_flg = 0 and b.del_flg = 0 and c.del_flg = 0 order by b.on_air_date asc")
    List<PM> findByTeamIdFuture(Long pmId);
}
