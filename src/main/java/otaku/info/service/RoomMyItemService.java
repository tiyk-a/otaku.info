package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.RoomMyItem;
import otaku.info.repository.RoomMyItemRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class RoomMyItemService {

    @Autowired
    RoomMyItemRepository roomMyItemRepository;

    public RoomMyItem save(RoomMyItem roomMyItem) {
        return roomMyItemRepository.save(roomMyItem);
    }

    /**
     * そのItemIdのオブジェクトを返す
     *
     * @param itemId
     * @return
     */
    public RoomMyItem findByItemId(String itemId) {
        return roomMyItemRepository.findByItemId(itemId).orElse(null);
    }

    /**
     * いいね数に変動があった商品を集める
     * @return
     */
    public List<RoomMyItem> findUpdTarget() {
        return roomMyItemRepository.findUpdTarget();
    }
}
