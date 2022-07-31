package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Cast;
import otaku.info.repository.CastRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class CastService {

    private CastRepository castRepository;

    public Cast save(Cast cast) {
        return castRepository.save(cast);
    }

    public List<Cast> saveAll(List<Cast> castList) {
        return castRepository.saveAll(castList);
    }

    public List<Cast> findByRegPmId(Long regPmId) {
        if (regPmId == null) {
            return new ArrayList<>();
        }
        return castRepository.findByRegPmId(regPmId);
    }

    public Boolean existData(Long regPmId, Long tmId) {
        return castRepository.existData(regPmId, tmId) > 0;
    }

    public List<Long> findIdListByRegPmId(Long regPmId) {
        return castRepository.findIdListByRegPmId(regPmId);
    }
}
