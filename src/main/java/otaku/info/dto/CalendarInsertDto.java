package otaku.info.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * GoogleカレンダーInsertのためのDTO
 */
@Setter
@Getter
public class CalendarInsertDto {

    private Date startDate;

    private Date endDate;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String title;

    private String desc;

    private Boolean allDayFlg;
}
