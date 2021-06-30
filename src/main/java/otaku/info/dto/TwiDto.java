package otaku.info.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TwiDto {

    public String title;

    public String url;

    public Date publication_date;

    public Date reserve_due;
}
