package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.PMRelMem;
import otaku.info.repository.PMRelMemRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PMRelMemService {

    @Autowired
    PMRelMemRepository pmRelMemRepository;

    public List<PMRelMem> findByPRelId(Long pmRelId) {
        return pmRelMemRepository.findByPRelId(pmRelId);
    }
}
