package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Cast;

import java.util.List;

public interface CastRepository extends JpaRepository<Cast, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM cast c where c.reg_pm_id = ?1")
    List<Cast> findByRegPmId(Long regPmId);

    @Query(nativeQuery = true, value = "SELECT count(*) FROM cast c WHERE regular_pm_id = ?1 and tm_id = ?2")
    int existData(Long regPmId, Long tmId);
}
