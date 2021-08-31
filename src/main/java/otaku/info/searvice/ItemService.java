package otaku.info.searvice;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Item;
import otaku.info.repository.ItemRepository;
import otaku.info.utils.DateUtils;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 商品テーブルのサービス
 *
 */
@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ItemService {

    private DateUtils dateUtils;

    private final ItemRepository itemRepository;

    public List<String> tmpMethod() {
        return itemRepository.tmpMethod();
    }

    public Long findItemId(String code) {
        return itemRepository.findItemIdByItemCode(code);
    }

    public void updateItem(Item item) {
        itemRepository.saveAndFlush(item);
    }
    public Item saveItem(Item item) {
        if (!hasData(item.getItem_code())) {
            return itemRepository.saveAndFlush(item);
        }
        return new Item();
    }

    public List<Item> saveAll(List<Item> itemList) {
        return itemRepository.saveAll(itemList);
    }

    public List<String> findNewItemList(List<String> searchItemList) {
        List<String> existItemCodeList = itemRepository.findItemCodeList(searchItemList);
        searchItemList.removeAll(existItemCodeList);
        return searchItemList;
    }

    public boolean hasData(String itemCode) {
        Long result = itemRepository.hasItemCode(itemCode);
        return result!=0;
    }

    public Optional<Item> findByItemId(Long itemId) {
        return itemRepository.findById(itemId);
    }

    public void flush() {
        itemRepository.flush();
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public List<Item> findFutureItemByDate(Integer days) {
        return itemRepository.findFutureItemByDate(dateUtils.daysAfterToday(days));
    }

    public List<Item> findReleasedItemList() {
        return itemRepository.findReleasedItemList();
    }

    public void deleteByItemId(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    public boolean waitingFctChk() {
        Long result = itemRepository.waitingFctChk();
        return result!=0;
    }

    public List<Item> findByFctChk(boolean isFctChked) {
        return itemRepository.findByFctChk(isFctChked);
    }

    public boolean deleteByItemIdList(List<Integer> idList) {
        List<Long> longIdList = new ArrayList<>();
        idList.forEach(e -> longIdList.add(Long.valueOf(e)));
        int result = itemRepository.deleteByItemIdList(longIdList);
        return longIdList.size() == result;
    }

    public List<Long> getItemIdListByDlt_flg(List<Integer> idList, boolean del_flg) {
        List<Long> longIdList = new ArrayList<>();
        idList.forEach(e -> longIdList.add(Long.valueOf(e)));
        return itemRepository.getNotDeletedItemIdList(longIdList, del_flg);
    }

    public boolean updateAllPublicationDate(Map<Long, Date> map) {
        boolean successFlg = true;
        for (Map.Entry<Long, Date> m : map.entrySet()) {
            int result = itemRepository.updateAllPublicationDate(m.getKey(), m.getValue());
            if (result == 0) {
                successFlg = false;
            }
        }
        return successFlg;
    }

    public List<Long> getItemIdListNotUpdated(Map<Long, Date> map) {
        List<Long> leftItemIdList = new ArrayList<>();
        for (Map.Entry<Long, Date> m : map.entrySet()) {
            if (itemRepository.getItemIdListNotUpdated(m.getKey(), m.getValue()) != 0) {
                leftItemIdList.add(m.getKey());
            }
        }
        return leftItemIdList;
    }

    public List<Item> findItemsBetween(Date from, Date to) {
        return itemRepository.findItemsBetween(from, to);
    }

    public Optional<Item> findByItemCode(String itemCode) {
        return itemRepository.findByItemCode(itemCode);
    }

    public List<Item> getDuplMemberItemList(List<Long> memberIdList) {
        List<Item> returnList = new ArrayList<>();
        for (Long id : memberIdList) {
            for (Item item : itemRepository.getDuplMemberItemList(id.toString())) {
                if (!returnList.contains(item)) {
                    returnList.add(item);
                }
            }
        }
        return returnList;
    }

    public void updateAll(List<Item> itemList) {
        itemRepository.saveAll(itemList);
    }
}
