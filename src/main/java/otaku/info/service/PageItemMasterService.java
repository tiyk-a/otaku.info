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
        return pageItemMasterRepository.findAll(PageRequest.of(page, pageSize, Sort.by("item_m_id").descending()));
    }

    public ItemMaster findById(Long imId) {
        return pageItemMasterRepository.findById(imId).orElse(new ItemMaster());
    }

    public ItemMaster save(ItemMaster im) {
        return pageItemMasterRepository.save(im);
    }
}
