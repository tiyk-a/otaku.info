package otaku.info.dto;

import com.google.api.client.util.DateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * GoogleカレンダーInsertのためのDTO
 */
@Setter
@Getter
public class CalendarInsertDto {

    private DateTime startDate;

    private DateTime endDate;

    private String title;

    private String desc;

    private Boolean allDayFlg;
}
