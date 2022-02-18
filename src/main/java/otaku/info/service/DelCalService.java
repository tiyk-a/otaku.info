package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.DelCal;
import otaku.info.repository.DelCalRepository;

import javax.transaction.Transactional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class DelCalService {

    @Autowired
    DelCalRepository delCalRepository;

    public DelCal save(DelCal delCal) {
        return delCalRepository.save(delCal);
    }
}
