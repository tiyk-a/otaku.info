//package otaku.info.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import otaku.info.entity.PCast;
//
//import java.util.List;
//
//public interface PCastRepository extends JpaRepository<PCast, Long> {
//
//    @Query(nativeQuery = true, value = "select * from p_cast where program_id = ?1")
//    List<PCast> findByProgramId(Long programId);
//}
