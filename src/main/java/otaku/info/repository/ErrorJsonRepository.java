package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.ErrorJson;

import java.util.List;

public interface ErrorJsonRepository extends JpaRepository<ErrorJson, Long> {

    @Query("select t from error_json t where json = ?1")
    ErrorJson findByJson(String json);

    @Query("select t from error_json t where is_solved = false")
    List<ErrorJson> isNotSolved();

    @Query("select t from error_json t where team_id = ?1 and is_solved = false")
    List<ErrorJson> findByTeamIdNotSolved(Long teamId);
}
