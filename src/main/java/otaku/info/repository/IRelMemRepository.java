package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.IRelMem;
import otaku.info.entity.IRelMemKey;

import java.util.List;

public interface IRelMemRepository extends JpaRepository<IRelMem, IRelMemKey> {

    @Query("select t from i_rel_mem t where i_rel_id = ?1")
    List<IRelMem> findByIRelId(Long itemId);
}
