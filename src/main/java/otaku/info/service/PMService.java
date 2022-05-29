package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.PM;
import otaku.info.repository.PMRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PMService {

    @Autowired
    PMRepository pmRepository;

    public List<PM> findByTeamIdFuture(Long teamId) {
        return pmRepository.findByTeamIdFuture(teamId);
    }
}
