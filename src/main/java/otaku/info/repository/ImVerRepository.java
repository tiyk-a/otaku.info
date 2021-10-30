package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.ImVer;


public interface ImVerRepository extends JpaRepository<ImVer, Long> {

    @Query(nativeQuery = true, value = "select count(*) from im_ver a inner join im_rel b on a.im_rel = b.im_id where ver_name = ?1 and b.team_id = ?2")
    int existtVerNameImId(String verName, Long teamId);
}
