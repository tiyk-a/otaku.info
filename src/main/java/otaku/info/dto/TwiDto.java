package otaku.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Twitter用、あまり活用できてない
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwiDto {

    public String title;

    public String url;

    public Date publication_date;

    public Date reserve_due;

    public Long team_id;

    public List<String> memList;
}
