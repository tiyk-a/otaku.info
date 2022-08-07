package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import otaku.info.entity.GCalendar;

public interface GCalendarRepository extends JpaRepository<GCalendar, Long> {
}
