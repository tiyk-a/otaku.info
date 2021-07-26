package otaku.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbNotifDto {

    private String data;
    private Date date;
    private LocalDateTime localDateTime;
}
