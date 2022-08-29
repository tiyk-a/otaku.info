package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.dto.PmFullDto;
import otaku.info.entity.PM;
import otaku.info.repository.PMRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PMService {

    @Autowired
    PMRepository pmRepository;

    public List<PM> findByStationIdNotNull() {
        return pmRepository.findByStationIdNotNull();
    }

    public List<PM> tmpMethod() {
        return pmRepository.tmpMethod();
    }

    public List<PM> tmpMethod2() {
        return pmRepository.tmpMethod2();
    }

    public List<PM> findByRelIdNotNull () {
        return pmRepository.findByRelIdNotNull();
    }

    public List<PM> findbyInvalidArr() {
        return pmRepository.findbyInvalidArr();
    }

    public PM save(PM pm) {
        return pmRepository.save(pm);
    }

    public List<PM> saveAll(List<PM> pmList) {
        return pmRepository.saveAll(pmList);
    }

    public PM findByPmId(Long pmId) {
        return pmRepository.findByPmId(pmId);
    }

    public List<PM> findbyPmIdList(List<Long> pmIdList) {
        return pmRepository.findbyPmIdList(pmIdList);
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

    public List<PM> findByOnAirDateNotDeleted(Date date) {
        return pmRepository.findByOnAirDateNotDeleted(date);
    }

    public List<PM> findByOnAirDateNotDeleted(LocalDateTime dateTime, Integer hour) {
        LocalDateTime endTime = dateTime.plusHours(hour);
        return pmRepository.findByOnAirDateNotDeleted(dateTime, endTime);
    }

    public List<PM> findByOnAirDateNotDeleted(Date sDate, Date eDate) {
        return pmRepository.findByOnAirDateNotDeleted(sDate, eDate);
    }

    public List<PM> findByOnAirDateNotDeletedTeamId(Date date, Long teamId) {
        return pmRepository.findByOnAirDateNotDeletedTeamId(date, teamId);
    }
}
