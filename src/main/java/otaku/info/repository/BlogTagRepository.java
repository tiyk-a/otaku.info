package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.BlogTag;

import java.util.Optional;

public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {

    @Query("select wp_tag_id from blog_tag where tag_name = ?1 and team_id = ?2")
    Optional<Long> findBlogTagIdByTagName(String tagName, Long teamId);

    @Query("select count(t) from blog_tag t where tag_name = ?1")
    int exists(String tagName);

    @Query(nativeQuery = true, value = "select * from blog_tag where tag_name = ?1 limit 1")
    BlogTag findByTagName(String tagName);
}
