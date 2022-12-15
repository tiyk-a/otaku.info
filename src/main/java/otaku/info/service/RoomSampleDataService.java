package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.RoomSampleData;
import otaku.info.repository.RoomSampleDataRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class RoomSampleDataService {

    private final RoomSampleDataRepository roomSampleDataRepository;

    public RoomSampleData findByDataId(String dataId, String userId) {
        return roomSampleDataRepository.findByDataId(dataId, userId);
    }

    public RoomSampleData save(RoomSampleData roomSampleData) {
        return roomSampleDataRepository.save(roomSampleData);
    }

    public List<String> findUserIdList() {
        return roomSampleDataRepository.findUserIdList();
    }
}
