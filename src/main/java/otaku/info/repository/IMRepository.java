package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.IM;

import java.util.Date;
import java.util.List;

public interface IMRepository extends JpaRepository<IM, Long> {

    @Query("select t from im t inner join im_rel b on t.im_id = b.im_id where b.team_id = ?1 and t.publication_date >= CURRENT_DATE")
    List<IM> findByTeamIdFuture(Long teamId);

//    @Query(nativeQuery = true, value = "select a.* from im a inner join im_rel b on a.im_id = b.im_id where b.team_id = ?1 and publication_date > CURRENT_DATE order by publication_date limit 5")
    @Query(nativeQuery = true, value = "select a.* from im a inner join im_rel b on a.im_id = b.im_id where b.team_id = ?1 and publication_date > CURRENT_DATE AND ((DATEDIFF(publication_date, CURRENT_DATE) <= 8)  OR (DATEDIFF(publication_date, CURRENT_DATE) % 5 = 0)) order by publication_date")
    List<IM> findNearFutureIMByTeamId(Long teamId);

    @Query("SELECT t FROM im t WHERE publication_date = CURRENT_DATE and del_flg = 0")
    List<IM> findReleasedItemList();

    @Query(nativeQuery = true, value = "select a.* from im a inner join im_rel b on a.im_id = b.im_id where b.team_id = ?1 and a.del_flg = 0 and publication_date >= CURRENT_DATE and a.del_flg = 0 order by publication_date desc")
    List<IM> findByTeamIdNotDeleted(Long teamId);

    @Query(nativeQuery = true, value = "select count(*) from im where im_id = ?1")
    int exists(Long imId);

    @Query(nativeQuery = true, value = "select * from im t where publication_date >= ?1 and publication_date <= ?2 and del_flg = ?3")
    List<IM> findBetweenDelFlg(Date from, Date to, boolean delFlg);

    @Query(nativeQuery = true, value = "select a.* from im a inner join im_rel b on a.im_id = b.im_id where publication_date >= ?1 and b.team_id = ?2 limit ?3")
    List<IM> findDateAfterTeamIdLimit(Date from, Long teamId, Long limit);

    @Query("select t from im t inner join im_rel b on t.im_id = b.im_id where b.team_id = ?1 and t.publication_date = ?2")
    List<IM> findByTeamIdDate(Long teamId, Date date);

    @Query(nativeQuery = true, value = "select a.* from im a inner join im_rel b on a.im_id = b.im_id where a.title like %?1% and b.team_id != ?2")
    List<IM> findByKeyExcludeTeamId(String key, Long excludeTeamId);
}
