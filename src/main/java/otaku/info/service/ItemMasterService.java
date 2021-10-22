package otaku.info.service;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.Item;
import otaku.info.entity.ItemMaster;
import otaku.info.repository.ItemMasterRepository;
import otaku.info.setting.Log4jUtils;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ItemMasterService {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("ItemMasterService");

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
        logger.debug("IM新規登録：imId=" + savedItemMaster.getItem_m_id());
        item.setItem_m_id(savedItemMaster.getItem_m_id());
        Item savedItem = itemService.saveItem(item);
        logger.debug("Itemのitem_m_id=" + savedItem.getItem_m_id());
        return Collections.singletonMap(savedItemMaster, savedItem);
    }

    public ItemMaster findById(Long itemMasterId) {
        return itemMasterRepository.findById(itemMasterId).orElse(new ItemMaster());
    }

    public ItemMaster save(ItemMaster itemMaster) {
        ItemMaster im = itemMasterRepository.save(itemMaster);
        logger.debug("IM登録:" + im.getItem_m_id());
        logger.debug("ここではitem.item_m_idの登録は行いません");
        return im;
    }

    public List<ItemMaster> findReleasedItemList() {
        return itemMasterRepository.findReleasedItemList();
    }

    public List<ItemMaster> findItemsBetweenDelFlg(Date from, Date to, boolean delFlg) {
        return itemMasterRepository.findItemsBetweenDelFlg(from, to, delFlg);
    }

    public List<ItemMaster> saveAll(List<ItemMaster> itemMasterList) {
        List<ItemMaster> imList = itemMasterRepository.saveAll(itemMasterList);
        logger.debug("IMリスト登録しました");
        logger.debug("ここではitem.item_m_idの登録は行いません");
        return imList;
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
