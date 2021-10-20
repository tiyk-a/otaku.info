package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import otaku.info.entity.Program;
import otaku.info.repository.PageTvRepository;

import javax.transaction.Transactional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PageTvService {

    @Autowired
    PageTvRepository pageTvRepository;

    public Page<Program> findAll(Integer page, Integer pageSize) {
        return pageTvRepository.findAll(PageRequest.of(page, pageSize, Sort.by("program_id").descending()));
    }

    public Program findById(Long pId) {
        return pageTvRepository.findById(pId).orElse(new Program());
    }

    public Program save(Program p) {
        return pageTvRepository.save(p);
    }
}
