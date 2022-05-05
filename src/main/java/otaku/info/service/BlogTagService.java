package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.BlogTag;
import otaku.info.repository.BlogTagRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class BlogTagService {

    @Autowired
    BlogTagRepository blogTagRepository;

    public Optional<Long> findBlogTagIdByTagName(String tagName, Long teamId) {
        return blogTagRepository.findBlogTagIdByTagName(tagName, teamId);
    }

    public void saveAll(List<BlogTag> blogTagList) {
        blogTagRepository.saveAll(blogTagList);
    }

    public void saveIfNotSaved(List<BlogTag> blogTagList) {
        for (BlogTag blogTag : blogTagList) {
            boolean exists = blogTagRepository.exists(blogTag.getTag_name()) > 0;
            if (!exists) {
                blogTagRepository.save(blogTag);
            }
        }
    }

    public BlogTag save(BlogTag blogTag) {
        return blogTagRepository.save(blogTag);
    }

    public BlogTag findByTagName(String tagName) {
        BlogTag blogTag = blogTagRepository.findByTagName(tagName);

        if (blogTag == null) {
            blogTag = new BlogTag();
        }
        return blogTag;
    }
}
