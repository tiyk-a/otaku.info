package otaku.info.service;

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
    private final ItemService itemService;

    @Autowired
    private final ItemMasterRepository itemMasterRepository;

    public List<ItemMaster> findAll() {
        return itemMasterRepository.findAll();
    }

    public ItemMaster getMasterById(Long itemMasterId) {
        return itemMasterRepository.findById(itemMasterId).orElse(new ItemMaster());
    }

    /**
     * ItemからItemMaserを作成し、登録します。
     * textControllerをserviceに入れたくないため、このメソッド使用時は第２引数にitemMaster.titleに登録したいStringを入れる
     * (上記はtextController.createItemMasterTitle(itemList（第１引数のitemをリストにする）, item.getPublication_date()))で作成可能)
     *
     * @param item
     * @return
     */
    public Map<ItemMaster, Item> addByItem(Item item, String requestTitle) {
        // item -> itemMaster,引数のタイトル作成のためリストにItemを入れる
        List<Item> itemList = new ArrayList<>();
        itemList.add(item);
        ItemMaster itemMaster = item.convertToItemMaster(requestTitle);

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

    public ItemMaster findByWpId(Integer wpId) {
        return itemMasterRepository.findByWpId(wpId);
    }

    public List<ItemMaster> findByPublicationYear(Integer year) {
        return itemMasterRepository.findByPublicationYear(year);
    }

    public List<ItemMaster> findByWpIdUrlNullList(List<Integer> wpIdList) {
        return itemMasterRepository.findByWpIdUrlNullList(wpIdList);
    }

    public List<ItemMaster> findByPublicationYearWpIdNull(Integer year) {
        return itemMasterRepository.findByPublicationYearWpIdNull(year);
    }

    public List<ItemMaster> findAllNotPosted() {
        return itemMasterRepository.findAllNotPosted();
    }

    public List<ItemMaster> findDateAfterTeamIdLimit(Date from, Long teamId, Long limit) {
        return itemMasterRepository.findDateAfterTeamIdLimit(from, teamId, limit);
    }

    public List<ItemMaster> findNearFutureIMByTeamId(Long teamId) {
        return itemMasterRepository.findNearFutureIMByTeamId(teamId);
    }

    public List<ItemMaster> findByTeamId(Long teamId, Long limit) {
        return itemMasterRepository.findByTeamId(teamId, limit);
    }

    public boolean exists(Long imId) {
        return itemMasterRepository.existsById(imId);
    }
}
