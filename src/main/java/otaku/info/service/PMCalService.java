package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import otaku.info.entity.PMCal;
import otaku.info.repository.PMCalRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PMCalService {

    @Autowired
    private PMCalRepository pmCalRepository;

    public PMCal save(PMCal cal) {
        return pmCalRepository.save(cal);
    }

    public List<PMCal> saveAll(List<PMCal> calList) {
        return pmCalRepository.saveAll(calList);
    }

//    /**
//     * 使用なし
//     *
//     * @param verIdList
//     * @param relIdList
//     * @param delFlg
//     * @return
//     */
//    public List<PMCal> findByVerIdListRelIdListDelFlg(List<Long> verIdList, List<Long> relIdList, @Nullable Boolean delFlg) {
//        if (delFlg == null) {
//            return pmCalRepository.findByVerIdListRelIdList(verIdList, relIdList);
//        } else {
//            return pmCalRepository.findByVerIdListRelIdListDelFlg(verIdList, relIdList, delFlg);
//        }
//    }

    public List<PMCal> findByVerIdListTeamIdListDelFlg(List<Long> verIdList, List<Long> teamIdList, @Nullable Boolean delFlg) {
        if (delFlg == null) {
            return pmCalRepository.findByVerIdListTeamIdList(verIdList, teamIdList);
        } else {
            return pmCalRepository.findByVerIdListTeamIdListDelFlg(verIdList, teamIdList, delFlg);
        }
    }

//    public PMCal findByVerIdRelIdDelFlg(Long verId, Long relId, Boolean delFlg) {
//        return pmCalRepository.findByVerIdRelIdDelFlg(verId, relId, delFlg).orElse(null);
//    }
}
