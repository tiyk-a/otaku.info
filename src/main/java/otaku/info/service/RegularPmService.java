package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.RegularPM;
import otaku.info.repository.RegularPmRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class RegularPmService {

    private RegularPmRepository regularPmRepository;

    public RegularPM save(RegularPM regularPM) {
        return regularPmRepository.save(regularPM);
    }

    /**
     * 引数(title)の一致する既存データの有無をチェックする
     *
     * @param title
     * @return
     */
    public Boolean existData(String title) {
        return regularPmRepository.existData(title) > 0;
    }

    public RegularPM findById(Long regPmId) {
        return regularPmRepository.findById(regPmId).orElse(null);
    }

    public List<RegularPM> findByTeamId(Long teamId) {
       return regularPmRepository.findByTeamId(teamId);
    }
}
