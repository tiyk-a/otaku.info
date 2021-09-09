package otaku.info.searvice;

import java.util.*;

import lombok.AllArgsConstructor;
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

    private DateUtils dateUtils;

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

    public List<Item> findItemsBetweenDelFlg(Date from, Date to, boolean delFlg) {
        return itemRepository.findItemsBetweenDelFlg(from, to, delFlg);
    }

    public Optional<Item> findByItemCode(String itemCode) {
        return itemRepository.findByItemCode(itemCode);
    }

    public List<Item> findByDelFlg(boolean delFlg) {
        return itemRepository.findByDelFlg(delFlg);
    }

//    public List<Item> tmpMethod1() {
//        return itemRepository.tmpMethod1();
//    }

    public List<Item> findWpIdNotNullUpdatedAt(Date from) {
        return itemRepository.findWpIdNotNullUpdatedAt(from);
    }

    public List<Item> findNotDeleted() {
        return itemRepository.findNotDeleted();
    }

    /**
     * 引数の商品と同じ発売日＆teamIdが合致する商品リストを返却する
     *
     * @param item
     * @return
     */
    public List<Item> findSimilarItemList(Item item) {
        List<String> teamIdList = new ArrayList<>();
        List<Item> resultList = new ArrayList<>();

        // 商品のteamIdを抽出
        Arrays.stream(item.getTeam_id().split(",")).forEach(e -> teamIdList.add(e));

        // それぞれのteamIdでマスタ商品の登録がある商品を探す
        for (String teamId : teamIdList) {
            // teamIdと発売日とマスタ登録ありで検索し、リストに入っていなかったら追加する
            itemRepository.findSimilarItemList(teamId, item.getPublication_date()).stream().filter(e -> !resultList.contains(e)).forEach(e -> resultList.add(e));
        }
        return resultList;
    }

    public List<Item> findByMasterId(Long itemMasterId) {
        return itemRepository.findByMasterId(itemMasterId);
    }
}

