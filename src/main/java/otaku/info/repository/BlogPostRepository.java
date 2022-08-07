package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.BlogPost;

import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    @Query(nativeQuery = true, value = "select * from blog_post where im_id = ?1 and blog_enum_id = ?2")
    Optional<BlogPost> findByImIdBlogEnumId(Long imId, Long blogEnumId);
}
