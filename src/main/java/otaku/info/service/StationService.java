package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Station;
import otaku.info.enums.StationEnum;
import otaku.info.repository.StationRepository;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    public Optional<Long> findStationId(String stationName) {
        Optional<Long> id = Arrays.stream(StationEnum.values()).filter(e -> e.getName().equals(stationName)).map(StationEnum::getId).findFirst();
        if (!id.isPresent()) {
            List<Long> idList = stationRepository.findStationId(stationName);
            if (idList.size() > 0) {
                id = Optional.ofNullable(idList.get(0));
            }
        }
        return id;
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

    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    public List<Station> findByName(String name) {
        return stationRepository.findByName(name);
    }

    public Station findById(Long stationId) {
        return stationRepository.findById(stationId).orElse(null);
    }
}
