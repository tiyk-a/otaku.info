package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.BlogUpd;

import java.util.List;

public interface BlogUpdRepository extends JpaRepository<BlogUpd, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM blog_upd where created_at = updated_at")
    List<BlogUpd> findNotUpdated();
}
