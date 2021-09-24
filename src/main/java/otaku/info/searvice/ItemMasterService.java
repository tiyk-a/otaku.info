package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.Item;
import otaku.info.entity.ItemMaster;
import otaku.info.repository.ItemMasterRepository;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ItemMasterService {

    @Autowired
    private ItemService itemService;

    private ItemMasterRepository itemMasterRepository;

    public ItemMaster getMasterById(Long itemMasterId) {
        return itemMasterRepository.findById(itemMasterId).orElse(new ItemMaster());
    }

    public Map<ItemMaster, Item> addByItem(Item item) {
        ItemMaster itemMaster = item.convertToItemMaster();
        ItemMaster savedItemMaster = itemMasterRepository.save(itemMaster);
        item.setItem_m_id(savedItemMaster.getItem_m_id());
        Item savedItem = itemService.saveItem(item);
        return Collections.singletonMap(savedItemMaster, savedItem);
    }

    public ItemMaster findById(Long itemMasterId) {
        return itemMasterRepository.findById(itemMasterId).orElse(new ItemMaster());
    }

    public ItemMaster save(ItemMaster itemMaster) {
        return itemMasterRepository.save(itemMaster);
    }

    public List<ItemMaster> findReleasedItemList() {
        return itemMasterRepository.findReleasedItemList();
    }

    public List<ItemMaster> findItemsBetweenDelFlg(Date from, Date to, boolean delFlg) {
        return itemMasterRepository.findItemsBetweenDelFlg(from, to, delFlg);
    }

    public List<ItemMaster> saveAll(List<ItemMaster> itemMasterList) {
        return itemMasterRepository.saveAll(itemMasterList);
    }

    public List<ItemMaster> findWpIdNotNull() {
        return itemMasterRepository.findWpIdNotNull();
    }

    public ItemMaster findByWpId(Integer wpId) {
        return itemMasterRepository.findByWpId(wpId);
    }

    public List<ItemMaster> findByPublicationYear(Integer year) {
        return itemMasterRepository.findByPublicationYear(year);
    }

    public List<ItemMaster> findByWpIdList(List<Integer> wpIdList) {
        return itemMasterRepository.findByWpIdList(wpIdList);
    }

    public List<ItemMaster> findByWpIdUrlNullList(List<Integer> wpIdList) {
        return itemMasterRepository.findByWpIdUrlNullList(wpIdList);
    }
}
