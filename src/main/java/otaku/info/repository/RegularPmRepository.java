package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.RegularPM;

import java.util.List;

public interface RegularPmRepository extends JpaRepository<RegularPM, Long> {

    @Query(nativeQuery = true, value = "SELECT count(*) FROM regular_pm p WHERE title = ?1")
    int existData(String title);

    @Query(nativeQuery = true, value = "SELECT p.* FROM regular_pm p where FIND_IN_SET(?1, team_arr)")
    List<RegularPM> findByTeamId(Long teamId);

    @Query(nativeQuery = true, value = "select * from regular_pm where title like %?1% limit ?2")
    List<RegularPM> findByKeyLimit(String key, Long limit);
}
