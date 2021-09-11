package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.BlogTag;
import otaku.info.repository.BlogTagRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class BlogTagService {

    @Autowired
    BlogTagRepository blogTagRepository;

    public Integer findBlogTagIdByTagName(String tagName) {
        return blogTagRepository.findBlogTagIdByTagName(tagName).orElse(0);
    }

    public void saveAll(List<BlogTag> blogTagList) {
        blogTagRepository.saveAll(blogTagList);
    }

    public List<BlogTag> getBlogTagNotSavedOnInfoDb() {
        return blogTagRepository.getBlogTagNotSavedOnInfoDb();
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
}
