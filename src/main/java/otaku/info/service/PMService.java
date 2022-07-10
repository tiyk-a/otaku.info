package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.dto.PmFullDto;
import otaku.info.entity.PM;
import otaku.info.repository.PMRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PMService {

    @Autowired
    PMRepository pmRepository;

    public PM save(PM pm) {
        return pmRepository.save(pm);
    }

    public PM findByPmId(Long pmId) {
        return pmRepository.findByPmId(pmId);
    }

    public List<PM> findFutureDelFlg(Boolean delFlg) {
        return pmRepository.findFutureDelFlg(delFlg);
    }

    public List<PM> findByTeamIdFuture(Long teamId) {
        return pmRepository.findByTeamIdFuture(teamId);
    }

    public List<PM> findByTitleOnAirDate(String title, LocalDateTime date) {
        return pmRepository.findByTitleOnAirDate(title, date);
    }

    public List<PM> findByTitle(String title) {
        return pmRepository.findByTitle(title);
    }

    public List<PM> findByKeyLimit(String key, Integer limit) {
        return pmRepository.findByKeyLimit(key, limit);
    }

    /**
     *
     * @param ldt
     * @param stationId
     * @return
     */
    public List<PmFullDto> findPmFuByllDtoOnAirDateStationId(LocalDateTime ldt, Long stationId) {
        List<Object[]> res = pmRepository.findPmFuByllDtoOnAirDateStationId(ldt.toLocalDate(), stationId);
        return res.stream().map(PmFullDto::new).collect(Collectors.toList());
    }

    /**
     *
     * @param ldt
     * @param stationId
     * @return
     */
    public List<PmFullDto> findPmFuByllDtoOnAirDateExStationId(LocalDateTime ldt, Long stationId) {
        List<Object[]> res = pmRepository.findPmFuByllDtoOnAirDateExStationId(ldt, stationId);
        return res.stream().map(PmFullDto::new).collect(Collectors.toList());
    }

    public List<PmFullDto> findByOnAirDateNotDeleted(LocalDateTime ldt) {
        List<Object[]> res = pmRepository.findByOnAirDateNotDeleted(ldt);
        return res.stream().map(PmFullDto::new).collect(Collectors.toList());
    }
}
