package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.ErrorJson;

public interface ErrorJsonRepository extends JpaRepository<ErrorJson, Long> {

    @Query("select t from error_json t where json = ?1")
    ErrorJson findByJson(String json);
}
