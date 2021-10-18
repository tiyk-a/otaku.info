package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Station;
import otaku.info.enums.StationEnum;
import otaku.info.repository.StationRepository;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Optional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    public Optional<Long> findStationId(String stationName) {
        return Arrays.stream(StationEnum.values()).filter(e -> e.getName().equals(stationName)).map(e -> (long) e.getId()).findFirst();
    }

    public String getStationName(Long stationId) {
        return Arrays.stream(StationEnum.values()).filter(e -> e.getId().equals(Math.toIntExact(stationId))).map(StationEnum::getName).findFirst().orElse("");
    }

    /**
     * DBに登録します
     *
     * @param station
     * @return
     */
    public Station save(Station station) {
        return stationRepository.save(station);
    }
}
