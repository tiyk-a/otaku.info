package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.IMRelMem;
import otaku.info.entity.IMRelMemKey;

import java.util.List;
import java.util.Optional;

public interface IMRelMemRepository extends JpaRepository<IMRelMem, IMRelMemKey> {

    @Query("select t from im_rel_mem t where im_rel_mem_id = ?1")
    IMRelMem findByImRelMemId(Long imRelMemId);

    @Query("select t from im_rel_mem t where im_rel_id = ?1")
    List<IMRelMem> findByImRelId(Long imRelId);

    @Query(nativeQuery = true, value = "select count(*) from im_rel_mem where im_rel_id = ?1 and member_id = ?2")
    int countByIDS(Long relId, Long memId);

    @Query("select t from im_rel_mem t where im_rel_id = ?1 and member_id = ?2")
    Optional<IMRelMem> findByImRelIdMemId(Long relId, Long memId);
}
