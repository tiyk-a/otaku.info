package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.BlogUpd;
import otaku.info.repository.BlogUpdRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class BlogUpdService {

    @Autowired
    BlogUpdRepository blogUpdRepository;

    public BlogUpd save(BlogUpd blogUpd) {
        return blogUpdRepository.save(blogUpd);
    }

    public List<BlogUpd> saveAll(List<BlogUpd> blogUpdList) {
        return blogUpdRepository.saveAll(blogUpdList);
    }

    public List<BlogUpd> findNotUpdated() {
        return blogUpdRepository.findNotUpdated();
    }
}
