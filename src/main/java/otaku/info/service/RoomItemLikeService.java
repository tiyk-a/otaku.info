package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.RoomItemLike;
import otaku.info.repository.RoomItemLikeRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class RoomItemLikeService {

    @Autowired
    RoomItemLikeRepository roomItemLikeRepository;

    public RoomItemLike save (RoomItemLike roomItemLike) {
        return roomItemLikeRepository.save(roomItemLike);
    }

    public List<RoomItemLike> findAll() {
        return roomItemLikeRepository.findAll();
    }

    public List<RoomItemLike> findByItemId (String itemId) {
        return  roomItemLikeRepository.findByItemId(itemId);
    }

    /**
     * 1日以内に更新されたadded_userを探す
     * @return
     */
    public List<String> findByCreatedInADay() {
        return roomItemLikeRepository.findByCreatedInADay();
    }
}
