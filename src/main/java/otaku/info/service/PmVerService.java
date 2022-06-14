package otaku.info.service;

import com.sun.istack.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.PMVer;
import otaku.info.repository.PmVerRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PmVerService {

    private final PmVerRepository pmVerRepository;

    public PMVer save(PMVer ver) {
        return pmVerRepository.save(ver);
    }

    public List<PMVer> saveAll(List<PMVer> verList) {
        return pmVerRepository.saveAll(verList);
    }

    public PMVer findById(Long id) {
        return pmVerRepository.findById(id).orElse(null);
    }

    /**
     * pmIdが一致するverを全部取得します
     * del_flg = 0のものしか取得しない
     *
     * @param pmId
     * @return
     */
    public List<PMVer> findByPmIdDelFlg(Long pmId, @Nullable Boolean delFlg) {
        if (delFlg == null) {
            return pmVerRepository.findByPmId(pmId);
        } else {
            return pmVerRepository.findByPmIdDelFlg(pmId, delFlg);
        }
    }

    public List<PMVer> findByPmIdStationId(Long pmId, Long stationId) {
        return pmVerRepository.findByPmIdStationId(pmId, stationId);
    }

    public List<PMVer> findByOnAirDateNotDeleted(LocalDateTime dateTime, Integer hour) {
        LocalDateTime endTime = dateTime.plusHours(hour);
        return pmVerRepository.findByOnAirDateNotDeleted(dateTime, endTime);
    }

    public List<PMVer> findByOnAirDateNotDeleted(Date sDate, Date eDate) {
        return pmVerRepository.findByOnAirDateNotDeleted(sDate, eDate);
    }

    public List<PMVer> findByOnAirDateNotDeleted(Date date) {
        return pmVerRepository.findByOnAirDateNotDeleted(date);
    }

    public List<PMVer> findByOnAirDateNotDeletedTeamId(Date date, Long teamId) {
        return pmVerRepository.findByOnAirDateNotDeletedTeamId(date, teamId);
    }
}
