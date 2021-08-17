package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Item;
import otaku.info.repository.ItemRepository;
import otaku.info.utils.DateUtils;

import javax.transaction.Transactional;
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
    }
}
