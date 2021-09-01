package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import otaku.info.entity.Station;
import otaku.info.repository.StationRepository;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    final Logger logger = org.slf4j.LoggerFactory.getLogger(StationService.class);

    public Optional<Long> findStationId(String stationName) {
        logger.info("KOKOKOKOKOKO " + stationName);
        return stationRepository.findStationId(stationName);
    }

    public String getStationName(Long stationId) {
        return stationRepository.getStationName(stationId);
    }

    public Station save(Station station) {
        return stationRepository.save(station);
    }
}
