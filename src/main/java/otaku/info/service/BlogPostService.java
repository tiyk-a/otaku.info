package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.BlogPost;
import otaku.info.repository.BlogPostRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class BlogPostService {

    private BlogPostRepository blogPostRepository;

    public List<BlogPost> saveAll(List<BlogPost> blogPostList) {
        return blogPostRepository.saveAll(blogPostList);
    }

    /**
     * 既存データ見つからない場合はnewオブジェクト返します
     *
     * @param imId
     * @param blogEnumId
     * @return
     */
    public BlogPost findByImIdBlogEnumId(Long imId, Long blogEnumId) {
        return blogPostRepository.findByImIdBlogEnumId(imId, blogEnumId).orElse(new BlogPost());
    }
}
