package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.ImVer;

import java.util.List;


public interface ImVerRepository extends JpaRepository<ImVer, Long> {

    @Query(nativeQuery = true, value = "select count(*) from im_ver a inner join im b on a.im_id = b.im_id where ver_name = ?1 and FIND_IN_SET(?2, b.team_arr)")
    int existtVerNameImId(String verName, Long teamId);

    @Query("select t from im_ver t where im_id = ?1 and del_flg = false")
    List<ImVer> findByImId(Long imId);
}
