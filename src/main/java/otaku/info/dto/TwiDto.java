package otaku.info.dto;

import lombok.Data;

import java.util.Date;

/**
 * Twitter用、あまり活用できてない
 *
 */
@Data
public class TwiDto {

    public String title;

    public String url;

    public Date publication_date;

    public Date reserve_due;
}
