package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import otaku.info.entity.ItemMaster;
import otaku.info.repository.PageItemMasterRepository;

import javax.transaction.Transactional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PageItemMasterService {

    @Autowired
    PageItemMasterRepository pageItemMasterRepository;

    public Page<ItemMaster> findAll(Integer page, Integer pageSize) {
        return pageItemMasterRepository.findAll(PageRequest.of(page, pageSize, Sort.by("publication_date").descending()));
    }

    public ItemMaster findById(Long imId) {
        return pageItemMasterRepository.findById(imId).orElse(new ItemMaster());
    }

    public ItemMaster save(ItemMaster im) {
        return pageItemMasterRepository.save(im);
    }

    public Page<ItemMaster> findByTeamId(Integer page, Integer pageSize, Long teamId) {
        return pageItemMasterRepository.findByTeamId(PageRequest.of(page, pageSize, Sort.by("publication_date").descending()), teamId);
    }
}
