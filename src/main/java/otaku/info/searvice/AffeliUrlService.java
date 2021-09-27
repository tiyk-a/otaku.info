package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.AffeliUrl;
import otaku.info.repository.AffeliUrlRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class AffeliUrlService {

    @Autowired
    AffeliUrlRepository affeliUrlRepository;

    public void saveAll(List<AffeliUrl> affeliUrlList) {
        affeliUrlRepository.saveAll(affeliUrlList);
    }
}
