package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.PMRel;
import otaku.info.repository.PMRelRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PMRelService {

    @Autowired
    PMRelRepository pmRelRepository;

    public List<PMRel> findByPmId(Long pmId) {
        return pmRelRepository.findByPmId(pmId);
    }

}
