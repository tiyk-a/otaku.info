//package otaku.info.repository;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.PagingAndSortingRepository;
//import otaku.info.entity.Program;
//
//public interface PageTvRepository extends PagingAndSortingRepository<Program, Long> {
//
//    @Query("select t from program t order by on_air_date desc")
//    Page<Program> findAll(PageRequest req);
//}
