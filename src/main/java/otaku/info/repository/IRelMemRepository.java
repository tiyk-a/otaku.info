package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import otaku.info.entity.IRelMem;
import otaku.info.entity.IRelMemKey;

public interface IRelMemRepository extends JpaRepository<IRelMem, IRelMemKey> {
}
