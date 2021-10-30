package otaku.info.service;

import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.IM;
import otaku.info.repository.IMRepository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class IMService {

    @Autowired
    IMRepository imRepository;

    public IM save(IM im) {
        return imRepository.save(im);
    }

    public List<IM> findByTeamIdFuture(Long teamId) {
        return imRepository.findByTeamIdFuture(teamId);
    }

    public IM findById(Long id) {
        return imRepository.findById(id).orElse(new IM());
    }

    public List<IM> findNearFutureIMByTeamId(Long teamId) {
        return imRepository.findNearFutureIMByTeamId(teamId);
    }

    public List<IM> findReleasedItemList() {
        return imRepository.findReleasedItemList();
    }

    public List<IM> findByTeamIdNotDeleted(Long teamId) {
        return imRepository.findByTeamIdNotDeleted(teamId);
    }

    public boolean exists(Long imId) {
        return imRepository.exists(imId) > 0;
    }

    public List<IM> findBetweenDelFlg(Date from, Date to, Boolean delFlg) {
        return imRepository.findBetweenDelFlg(from, to, delFlg);
    }

    public List<IM> findDateAfterTeamIdLimit(Date from, Long teamId, Long limit) {
        return imRepository.findDateAfterTeamIdLimit(from, teamId, limit);
    }
}
