package otaku.info.service;

import java.util.*;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.Item;
import otaku.info.repository.ItemRepository;
import otaku.info.utils.DateUtils;

import javax.transaction.Transactional;

/**
 * 商品テーブルのサービス
 *
 */
@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ItemService {

    private final DateUtils dateUtils;

    @Autowired
    IRelService iRelService;

    private final ItemRepository itemRepository;

    public List<String> tmpMethod() {
        return itemRepository.tmpMethod();
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

    public List<Item> findByFctChk(boolean isFctChked) {
        return itemRepository.findByFctChk(isFctChked);
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

    public Optional<Item> findByItemCode(String itemCode) {
        return itemRepository.findByItemCode(itemCode);
    }

    public List<Item> findByDelFlg(boolean delFlg) {
        return itemRepository.findByDelFlg(delFlg);
    }

    public List<Item> findWpIdNotNullUpdatedAt(Date from) {
        return itemRepository.findWpIdNotNullUpdatedAt(from);
    }

    /**
     * 引数の商品と同じ発売日＆teamIdが合致する商品リストを返却する
     *
     * @param item
     * @return
     */
    public List<Item> findSimilarItemList(Item item) {
        List<Long> teamIdList = iRelService.getTeamIdListByItemId(item.getItem_id());
        List<Item> resultList = new ArrayList<>();

        // それぞれのteamIdでマスタ商品の登録がある商品を探す
        for (Long teamId : teamIdList) {
            // teamIdと発売日とマスタ登録ありで検索し、リストに入っていなかったら追加する
            itemRepository.findSimilarItemList(teamId, item.getPublication_date()).stream().filter(e -> !resultList.contains(e)).forEach(e -> resultList.add(e));
        }
        return resultList;
    }

    public List<Item> findByMasterId(Long itemMasterId) {
        return itemRepository.findByMasterId(itemMasterId);
    }

    public List<Item> gatherItems(Long itemMId) {
        return itemRepository.gatherItems(itemMId);
    }

    public boolean isRegistered(String code, Integer siteId) {
        return itemRepository.isRegistered(code, siteId) > 0;
    }

    public List<Item> findByMIdNullFuture() {
        return itemRepository.findByMIdNullFuture();
    }

    public List<Item> findByTeamIdNotDeleted(Long teamId) {
        return itemRepository.findByTeamIdNotDeleted(teamId);
    }
}

