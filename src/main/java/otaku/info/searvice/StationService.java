package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.repository.StationRepository;

import javax.transaction.Transactional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    public Long findStationId(String stationName) {
        return stationRepository.findStationId(stationName);
    }
}
