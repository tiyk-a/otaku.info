package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.RegularPM;

import java.util.List;

public interface RegularPmRepository extends JpaRepository<RegularPM, Long> {

    @Query(nativeQuery = true, value = "SELECT count(*) FROM regular_pm p WHERE title = ?1")
    int existData(String title);

    @Query(nativeQuery = true, value = "SELECT p.* FROM regular_pm p inner join cast c on p.regular_pm_id = c.regular_pm_id where c.tm_id = ?1")
    List<RegularPM> findByTeamId(Long teamId);
}
