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

    public String amazon_url;

    public String rakuten_url;

    public String blog_url;

    public Date publication_date;

    public Date reserve_due;

    public List<String> teamNameList;

    public List<String> memList;
}
