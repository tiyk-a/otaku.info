package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.PMVer;
import otaku.info.repository.PmVerRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PmVerService {

    private PmVerRepository pmVerRepository;

    /**
     * pmIdが一致するverを全部取得します
     * del_flg = 0のものしか取得しない
     *
     * @param pmId
     * @return
     */
    public List<PMVer> findByPmId(Long pmId) {
        return pmVerRepository.findByPmId(pmId);
    }
}
