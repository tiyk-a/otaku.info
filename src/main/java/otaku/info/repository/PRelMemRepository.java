//package otaku.info.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import otaku.info.entity.PRelMem;
//import otaku.info.entity.PRelMemKey;
//
//import java.util.List;
//
//public interface PRelMemRepository extends JpaRepository<PRelMem, PRelMemKey> {
//
//    @Query("select t from p_rel_mem t where p_rel_id = ?1")
//    List<PRelMem> findByPRelId(Long relId);
//}
