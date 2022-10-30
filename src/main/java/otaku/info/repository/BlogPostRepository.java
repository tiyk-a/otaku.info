package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.BlogPost;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    @Query(nativeQuery = true, value = "select * from blog_post where im_id = ?1")
    List<BlogPost> findByImId(Long imId);

    @Query(nativeQuery = true, value = "select * from blog_post where im_id = ?1 and blog_enum_id = ?2")
    List<BlogPost> findByImIdBlogEnumId(Long imId, Long blogEnumId);
}
