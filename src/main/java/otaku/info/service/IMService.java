package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.IM;
import otaku.info.repository.IMRepository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class IMService {

    @Autowired
    IMRepository imRepository;

    public List<IM> tmpMethod() {
        return imRepository.tmpMethod();
    }

    public List<IM> tmpMethod2() {
        return imRepository.tmpMethod2();
    }

    public IM save(IM im) {
        return imRepository.save(im);
    }

    public List<IM> saveAll(List<IM> imList) {
        return imRepository.saveAll(imList);
    }

    public List<IM> findByTeamIdFuture(Long teamId) {
        return imRepository.findByTeamIdFuture(teamId);
    }

    public List<IM> findByTeamIdFutureOrWpIdNull(Long teamId) {
        return imRepository.findByTeamIdFutureOrWpIdNull(teamId);
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

    public List<IM> findByTeamIdDate(Long teamId, Date date) {
        return imRepository.findByTeamIdDate(teamId, date);
    }

    /**
     *
     * @param key
     * @return
     */
    public List<IM> findByKeyExcludeTeamId(String key) {
        return imRepository.findByKeyExcludeTeamId(key);
    }

    /**
     * タイトルが一致するimを取得する
     *
     * @param title
     * @return
     */
    public List<IM> findByTitle(String title) {
        return imRepository.findByTitle(title);
    }

    public List<IM> findFuture() {
        return imRepository.findFuture();
    }

    /**
     * 引数teamidの直近1件のレコードを取得する
     * アフィリURLがあるもの優先
     * @param teamId
     * @return
     */
    public Optional<IM> findUpcomingImWithUrls(Long teamId) {
        return imRepository.findUpcomingImWithUrls(teamId);
    }
}
