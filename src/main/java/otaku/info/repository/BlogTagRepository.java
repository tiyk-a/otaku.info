package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.BlogTag;

import java.util.Optional;

public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {

    @Query("select wp_tag_id from blog_tag where tag_name = ?1")
    Optional<Integer> findBlogTagIdByTagName(String tagName);

//    @Query("select * from blog_tag where ")
//    List<BlogTag> getBlogTagNotSavedOnInfoDb();

    @Query("select count(t) from blog_tag t where tag_name = ?1")
    int exists(String tagName);
}
