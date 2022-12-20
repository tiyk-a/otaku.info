package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.RoomUser;
import otaku.info.repository.RoomUserRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class RoomUserService {

    @Autowired
    RoomUserRepository roomUserRepository;

    public RoomUser save(RoomUser roomUser) {
        return roomUserRepository.save(roomUser);
    }

    public RoomUser findByUserId(String roomUserId) {
        return roomUserRepository.findByUserId(roomUserId);
    }

    public String findUserNameByUserId(String userId) {
        return roomUserRepository.findUserNameByUserId(userId).orElse(null);
    }

    public List<String> findUserIdListByUserId(List<String> userIdList) {
        return roomUserRepository.findUserIdListByUserId(userIdList);
    }

    public int findLikeCountByUserId(String userId) {
        return roomUserRepository.findLikeCountByUserId(userId);
    }
}
