//package otaku.info.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import otaku.info.entity.PMCast;
//
//import java.util.List;
//
//public interface PmCastRepository extends JpaRepository<PMCast, Long> {
//
//    @Query(nativeQuery = true, value = "select * from pm_cast where pm_id = ?1")
//    List<PMCast> findByPmId(Long pmId);
//}
