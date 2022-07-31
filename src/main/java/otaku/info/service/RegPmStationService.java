package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.RegPmStation;
import otaku.info.repository.RegPmStationRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class RegPmStationService {

    private RegPmStationRepository regPmStationRepository;

    public void saveAll(List<RegPmStation> regPmStationList) {
        regPmStationRepository.saveAll(regPmStationList);
    }

    public RegPmStation save(RegPmStation regPmStation) {
        return regPmStationRepository.save(regPmStation);
    }

    /**
     * 引数(title)の一致する既存データの有無をチェックする
     *
     * @param regPmId
     * @param stationId
     * @return
     */
    public Boolean existData(Long regPmId, Long stationId) {
        return regPmStationRepository.existData(regPmId, stationId) > 0;
    }

    public List<Long> findStationIdListByReguPmId(Long regPmId) {
        return regPmStationRepository.findStationIdListByReguPmId(regPmId);
    }
}
