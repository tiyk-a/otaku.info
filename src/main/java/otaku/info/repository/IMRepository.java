package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.IM;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IMRepository extends JpaRepository<IM, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM im t WHERE publication_date >= '2022-01-01' and del_flg = 0 and team_arr is null limit 50")
    List<IM> tmpMethod();

    @Query(nativeQuery = true, value = "SELECT * FROM im t WHERE publication_date < '2022-01-01' and del_flg = 0 and team_arr is null limit 50")
    List<IM> tmpMethod2();

    @Query(nativeQuery = true, value = "select * from im t where FIND_IN_SET(?1, team_arr) and t.publication_date >= CURRENT_DATE and t.del_flg = false")
    List<IM> findByTeamIdFuture(Long teamId);

    @Query(nativeQuery = true, value = "select t.* from im t inner join blog_post b on t.im_id = b.im_id where FIND_IN_SET(?1, team_arr) and (t.publication_date >= CURRENT_DATE or b.wp_id is null) and t.del_flg = false")
    List<IM> findByTeamIdFutureOrWpIdNull(Long teamId);

    @Query(nativeQuery = true, value = "select a.* from im where FIND_IN_SET(?1, team_arr) and publication_date > CURRENT_DATE AND ((DATEDIFF(publication_date, CURRENT_DATE) <= 8)  OR (DATEDIFF(publication_date, CURRENT_DATE) % 5 = 0)) order by publication_date")
    List<IM> findNearFutureIMByTeamId(Long teamId);

    @Query("SELECT t FROM im t WHERE publication_date = CURRENT_DATE and del_flg = 0")
    List<IM> findReleasedItemList();

    @Query(nativeQuery = true, value = "select a.* from im where FIND_IN_SET(?1, team_arr) and a.del_flg = 0 and publication_date >= CURRENT_DATE and a.del_flg = 0 order by publication_date desc")
    List<IM> findByTeamIdNotDeleted(Long teamId);

    @Query(nativeQuery = true, value = "select count(*) from im where im_id = ?1")
    int exists(Long imId);

    @Query(nativeQuery = true, value = "select * from im t where publication_date >= ?1 and publication_date <= ?2 and del_flg = ?3")
    List<IM> findBetweenDelFlg(Date from, Date to, boolean delFlg);

    @Query(nativeQuery = true, value = "select a.* from im a where publication_date >= ?1 and FIND_IN_SET(?2, team_arr) limit ?3")
    List<IM> findDateAfterTeamIdLimit(Date from, Long teamId, Long limit);

    /**
     * REL廃止対応、
     * @param teamId
     * @param date
     * select * from im where FIND_IN_SET(32, mem_arr);
     * @return
     */
    @Query(nativeQuery = true, value = "select * from im t where FIND_IN_SET(?1, team_arr) and t.publication_date = ?2")
    List<IM> findByTeamIdDate(Long teamId, Date date);

    @Query(nativeQuery = true, value = "select * from im a where a.title like %?1%")
    List<IM> findByKeyExcludeTeamId(String key);

    @Query("select t from im t where title = ?1")
    List<IM> findByTitle(String title);

    @Query("select t from im t where publication_date >= CURRENT_DATE and del_flg = 0")
    List<IM> findFuture();

    @Query(nativeQuery = true, value = "select * from im a where a.del_flg = 0 and a.publication_date => CURRENT_DATE and a.amazon_image is not null order by a.publication_date asc limit 1")
    Optional<IM> findUpcomingImWithUrls(Long teamId);
}
