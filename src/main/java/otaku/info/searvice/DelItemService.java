package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.DelItem;
import otaku.info.entity.Item;
import otaku.info.repository.DelItemRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * 関係ない商品テーブルのサービス
 *
 */
@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class DelItemService {

    private final DelItemRepository delItemRepository;

    public List<String> findNewItemList(List<String> searchItemList) {
        List<String> existItemCodeList = delItemRepository.findItemCodeList(searchItemList);
        searchItemList.removeAll(existItemCodeList);
        return searchItemList;
    }

    public void saveAll(List<Item> saveList) {
            List<DelItem> delItemList = new ArrayList<>();
            saveList.forEach(e -> delItemList.add(e.convertToDelItem()));
            delItemRepository.saveAll(delItemList);
    }

    public DelItem saveItem(DelItem delItem) {
        return delItemRepository.save(delItem);
    }

    public boolean waitingFctChk() {
        Long result = delItemRepository.waitingFctChk();
        return result!=0;
    }

    public List<DelItem> findByFctChk(int i) {
        return delItemRepository.findByFctChk(i);
    }
}
