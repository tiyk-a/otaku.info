package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.RegPmStation;
import otaku.info.repository.RegPmStationRepository;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<Long, String> findStationIdListByReguPmId(Long regPmId) {
        Map<Long, String> resMap = new HashMap<>();

        List<Object[]> res = regPmStationRepository.findStationIdListByReguPmId(regPmId);
        for (Object[] obj : res ) {
            BigInteger stationId = (BigInteger) obj[0];
            String stationName = (String) obj[1];
            resMap.put(stationId.longValue(), stationName);
        }
        return resMap;
    }
}
