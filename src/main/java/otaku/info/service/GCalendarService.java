package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.GCalendar;
import otaku.info.repository.GCalendarRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class GCalendarService {

    private GCalendarRepository gCalendarRepository;

    public List<GCalendar> saveAll(List<GCalendar> gCalendarList) {
        return gCalendarRepository.saveAll(gCalendarList);
    }
}
